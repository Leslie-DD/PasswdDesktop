package passwds.repository

import kotlinx.coroutines.flow.MutableStateFlow
import model.Setting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.datasource.LocalDataSource
import passwds.datasource.RemoteDataSource
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult
import utils.AESUtil
import java.util.*

class PasswdRepository(
    private val remoteDataSource: RemoteDataSource = RemoteDataSource(),
    private val localDataSource: LocalDataSource = LocalDataSource()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val groups = MutableStateFlow<MutableList<Group>>(arrayListOf())
    val groupPasswds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())

    private fun MutableList<Passwd>.mapToPasswdsMap(
        secretKeyBytes: ByteArray = Base64.getDecoder().decode(Setting.secretKey.value)
    ): MutableMap<Int, MutableList<Passwd>> {
        val passwdsMapResult: MutableMap<Int, MutableList<Passwd>> = hashMapOf()
        this.forEach { passwd ->
            decodePasswd(passwd, secretKeyBytes)
            if (passwdsMapResult[passwd.groupId].isNullOrEmpty()) {
                passwdsMapResult[passwd.groupId] = arrayListOf()
            }
            passwdsMapResult[passwd.groupId]?.add(passwd)
        }
        return passwdsMapResult
    }

//    suspend fun fetchPasswds() {
//        remoteDataSource.fetchPasswds()
//            .onSuccess {
//                val passwdsMapResult = it.mapToPasswdsMap()
//                logger.info("$TAG fetchPasswds success, passwdsMap: ${passwdsMapResult.size}, $passwdsMapResult")
//                localDataSource.passwdsMap = passwdsMapResult
//            }.onFailure {
//                it.printStackTrace()
//            }
//    }

    suspend fun fetchGroups() {
        remoteDataSource.fetchGroups()
            .onSuccess {
                localDataSource.groups = it
                groups.emit(it)
            }.onFailure {
                groups.emit(arrayListOf())
                it.printStackTrace()
            }
    }

    suspend fun fetchGroupPasswds(groupId: Int) {
        groupPasswds.emit(localDataSource.getGroupPasswds(groupId))
    }


    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> {
        return remoteDataSource.loginByToken(
            username = username,
            token = token,
            secretKey = secretKey
        ).onSuccess { loginResult ->
            localDataSource.passwdsMap = loginResult.passwds.mapToPasswdsMap()
            localDataSource.groups = mutableListOf()
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> {
        return remoteDataSource.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess { loginResult ->
            localDataSource.passwdsMap = loginResult.passwds.mapToPasswdsMap()
            localDataSource.groups = mutableListOf()
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult?> {
        return remoteDataSource.register(
            username = username,
            password = password
        ).onSuccess { loginResult ->
            localDataSource.passwdsMap = mutableMapOf()
            localDataSource.groups = mutableListOf()
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Group> {
        var result = Result.failure<Group>(Throwable())
        remoteDataSource.newGroup(
            groupName = groupName,
            groupComment = groupComment
        ).onSuccess {
            val newGroup = Group(
                id = it,
                userId = Setting.userId.value,
                groupName = groupName,
                groupComment = groupComment
            )
            localDataSource.passwdsMap[it] = mutableListOf()
            groups.emit(localDataSource.groups.apply { add(newGroup) })
            groupPasswds.emit(arrayListOf())
            result = Result.success(newGroup)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun deleteGroup(
        groupId: Int
    ): Result<Group> {
        var result = Result.failure<Group>(Throwable())
        remoteDataSource.deleteGroup(
            groupId = groupId
        ).onSuccess {
            localDataSource.passwdsMap.remove(groupId)
            localDataSource.groups.find { group: Group -> group.id == groupId }?.let {
                localDataSource.groups.remove(it)
                result = Result.success(it)
            }
            groups.emit(localDataSource.groups)
            groupPasswds.emit(mutableListOf())
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Result<Group> {
        var result = Result.failure<Group>(Throwable())
        remoteDataSource.updateGroup(
            groupId = groupId,
            groupName = groupName,
            groupComment = groupComment
        ).onSuccess {
            localDataSource.groups.find { group: Group -> group.id == groupId }?.let {
                it.groupName = groupName
                it.groupComment = groupComment
            }
            groups.emit(localDataSource.groups)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun newPasswd(
        groupId: Int,
        title: String,
        usernameString: String,
        passwordString: String,
        link: String,
        comment: String,
    ): Result<Passwd> {
        val secretKey = Setting.secretKey.value
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = encode(secretKey, usernameString),
            passwordString = encode(secretKey, passwordString),
            link = link,
            comment = comment
        ).onSuccess {
            val newPasswd = Passwd(
                id = it,
                groupId = groupId,
                title = title,
                userId = Setting.userId.value,
                link = link,
                comment = comment,
                usernameString = usernameString,
                passwordString = passwordString
            )
            localDataSource.passwdsMap[groupId]?.add(newPasswd)
            localDataSource.passwdsMap[groupId]?.let { ss ->
                groupPasswds.emit(ss)
            }
            result = Result.success(newPasswd)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameStrValue: String?,
        passwordStrValue: String?,
        link: String?,
        comment: String?
    ): Result<Passwd> {
        val secretKey = Setting.secretKey.value
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.updatePasswd(
            id = id,
            title = title,
            usernameStr = encode(secretKey, usernameStrValue ?: ""),
            passwordStr = encode(secretKey, passwordStrValue ?: ""),
            link = link,
            comment = comment
        ).onSuccess {
            getPasswdById(id)?.let { originPasswd ->
                originPasswd.apply {
                    this.title = title
                    this.usernameString = usernameStrValue
                    this.passwordString = passwordStrValue
                    this.link = link
                    this.comment = comment
                }
                getGroupPasswdsByGroupId(originPasswd.groupId)?.let { ss ->
                    groupPasswds.emit(ss)
                }
                result = Result.success(originPasswd)
            }
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun deletePasswd(id: Int): Result<Passwd> {
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.deletePasswd(id = id)
            .onSuccess {
                getPasswdById(id)?.let { passwd ->
                    getGroupPasswdsByGroupId(passwd.groupId)?.let { groupPasswdsss ->
                        groupPasswdsss.remove(passwd)
                        groupPasswds.emit(groupPasswdsss)
                        result = Result.success(passwd)
                    }
                }
            }.onFailure {
                result = Result.failure(it)
            }
        return result
    }

    fun getAllPasswds(value: String): MutableList<Passwd> {
        val pattern = Regex(PATTERN_PREFIX + value + PATTERN_SUFFIX)
        val result = arrayListOf<Passwd>()
        localDataSource.passwdsMap
            .flatMap { it.value }
            .forEach { passwd ->
                if (passwd.title?.matches(pattern) == true || passwd.usernameString?.matches(pattern) == true) {
                    result.add(passwd)
                }
            }
        return result
    }

    private fun decodePasswd(passwd: Passwd, secretKeyBytes: ByteArray?): Passwd {
        logger.debug("fetchGroupPasswd decode before: passwd: {}", passwd)
        try {
            passwd.usernameString?.let {
                passwd.usernameString = decode(it, secretKeyBytes)
            }
            passwd.passwordString?.let {
                passwd.passwordString = decode(it, secretKeyBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logger.debug("fetchGroupPasswd decode after: passwd: {}", passwd)
        return passwd
    }

    private fun decode(value: String, secretKeyBytes: ByteArray?): String {
        return if (value.isBlank()) {
            value
        } else {
            val decodePasswordResult =
                AESUtil.decrypt(secretKeyBytes, Base64.getDecoder().decode(value))
            if (decodePasswordResult != null) {
                String(decodePasswordResult)
            } else {
                value
            }
        }
    }

    private fun encode(secretKey: String, value: String): String {
        return if (value.isBlank()) {
            value
        } else {
            Base64.getEncoder().encodeToString(AESUtil.encrypt(secretKey, value))
        }
    }


    private fun getPasswdById(id: Int): Passwd? {
        return localDataSource.passwdsMap.flatMap { it.value }.find { passwd -> passwd.id == id }
    }

    private fun getGroupPasswdsByGroupId(id: Int): MutableList<Passwd>? {
        return localDataSource.passwdsMap[id]
    }

    companion object {
        private const val TAG = "PasswdRepository"
        private const val PATTERN_PREFIX = "^.*(?i)"
        private const val PATTERN_SUFFIX = ".*"
    }
}