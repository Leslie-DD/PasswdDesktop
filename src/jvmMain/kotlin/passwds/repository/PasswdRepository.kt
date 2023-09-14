package passwds.repository

import database.DataBase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val groupsFlow: StateFlow<MutableList<Group>>
        get() = localDataSource.groups.asStateFlow()

    val groupPasswdsFlow: StateFlow<MutableList<Passwd>>
        get() = localDataSource.groupPasswds.asStateFlow()

    private fun MutableList<Passwd>.mapToPasswdsMap(
        secretKey: String
    ): MutableMap<Int, MutableList<Passwd>> {
        val secretKeyByteArray = Base64.getDecoder().decode(secretKey)
        val passwdsMapResult: MutableMap<Int, MutableList<Passwd>> = hashMapOf()
        forEach { passwd ->
            decodePasswd(secretKeyByteArray, passwd)
            if (passwdsMapResult[passwd.groupId] == null) {
                passwdsMapResult[passwd.groupId] = arrayListOf()
            }
            passwdsMapResult[passwd.groupId]?.add(passwd)
        }
        return passwdsMapResult
    }

    suspend fun updateGroupPasswds(groupId: Int) {
        localDataSource.updateGroupPasswds(groupId)
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> {
        return remoteDataSource.loginByPassword(
            username = username,
            password = password,
        ).onSuccess { loginResult ->
            localDataSource.passwdsMap = loginResult.passwds.mapToPasswdsMap(secretKey)
            clearGroupAndGroupPasswds()
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun fetchGroups() {
        remoteDataSource.fetchGroups()
            .onSuccess { fetchedGroups ->
                localDataSource.updateGroups(fetchedGroups)
                fetchedGroups.forEach { group ->
                    if (localDataSource.passwdsMap[group.id] == null) {
                        localDataSource.passwdsMap[group.id] = arrayListOf()
                    }
                }
                if (fetchedGroups.isNotEmpty()) {
                    localDataSource.updateGroupPasswds(fetchedGroups.first().id)
                }
            }.onFailure {
                localDataSource.updateGroups(arrayListOf())
                localDataSource.updateGroupPasswds(arrayListOf())
                it.printStackTrace()
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
            clearGroupAndGroupPasswds()
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
                userId = DataBase.instance.globalUserId.value,
                groupName = groupName,
                groupComment = groupComment
            )
            localDataSource.passwdsMap[it] = mutableListOf()
            val originGroups = localDataSource.groups.value
            localDataSource.updateGroups(
                originGroups.apply {
                    add(newGroup)
                }
            )
            localDataSource.updateGroupPasswds(arrayListOf())
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
            val originGroups = localDataSource.groups.value
            originGroups.find { group: Group -> group.id == groupId }?.let {
                originGroups.remove(it)
                result = Result.success(it)
            }
            localDataSource.updateGroups(originGroups)
            localDataSource.updateGroupPasswds(arrayListOf())
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
            val originGroups = localDataSource.groups.value
            originGroups.find { group: Group -> group.id == groupId }?.let {
                it.groupName = groupName
                it.groupComment = groupComment
            }
            localDataSource.updateGroups(originGroups)
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
        val secretKeyByteArray = Base64.getDecoder().decode(DataBase.instance.globalSecretKey.value)
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.newPasswd(
            groupId = groupId,
            title = AESUtil.encrypt(secretKeyByteArray, title) ?: title,
            usernameString = AESUtil.encrypt(secretKeyByteArray, usernameString),
            passwordString = AESUtil.encrypt(secretKeyByteArray, passwordString),
            link = link,
            comment = comment
        ).onSuccess {
            val newPasswd = Passwd(
                id = it,
                groupId = groupId,
                title = title,
                userId = DataBase.instance.globalUserId.value,
                link = link,
                comment = comment,
                usernameString = usernameString,
                passwordString = passwordString
            )
            localDataSource.passwdsMap[groupId]?.add(newPasswd)
            localDataSource.updateGroupPasswds(groupId)

            result = Result.success(newPasswd)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameString: String?,
        passwordString: String?,
        link: String?,
        comment: String?
    ): Result<Passwd> {
        val secretKeyByteArray = Base64.getDecoder().decode(DataBase.instance.globalSecretKey.value)
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.updatePasswd(
            id = id,
            title = AESUtil.encrypt(secretKeyByteArray, title),
            usernameString = AESUtil.encrypt(secretKeyByteArray, usernameString),
            passwordString = AESUtil.encrypt(secretKeyByteArray, passwordString),
            link = link,
            comment = comment
        ).onSuccess {
            getPasswdById(id)?.let { originPasswd ->
                originPasswd.apply {
                    this.title = title
                    this.usernameString = usernameString
                    this.passwordString = passwordString
                    this.link = link
                    this.comment = comment
                }
                updateGroupPasswdsByGroupId(originPasswd.groupId)
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
                    updateGroupPasswdsByGroupId(
                        groupId = passwd.groupId,
                        onUpdated = {
                            result = Result.success(passwd)
                        }
                    ) { groupPasswdsss ->
                        groupPasswdsss.apply {
                            remove(passwd)
                        }
                    }
                }
            }.onFailure {
                result = Result.failure(it)
            }
        return result
    }

    suspend fun searchLikePasswdsAndUpdate(value: String) {
        if (value.isBlank()) {
            localDataSource.updateGroupPasswds(arrayListOf())
        } else {
            val pattern = Regex(PATTERN_PREFIX + value + PATTERN_SUFFIX)
            val result = arrayListOf<Passwd>()
            localDataSource.passwdsMap
                .flatMap { it.value }
                .forEach { passwd ->
                    if (passwd.title?.matches(pattern) == true || passwd.usernameString?.matches(pattern) == true) {
                        result.add(passwd)
                    }
                }
            localDataSource.updateGroupPasswds(result)
        }
    }

    private fun decodePasswd(
        secretKeyBytes: ByteArray? = null,
        passwd: Passwd
    ): Passwd {
//        logger.debug("fetchGroupPasswd decode before: passwd: {}", passwd)
        try {
            passwd.title = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.title)
            passwd.usernameString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.usernameString)
            passwd.passwordString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.passwordString)
        } catch (e: Exception) {
            logger.error("fetchGroupPasswd error " + e.message)
            e.printStackTrace()
        }
//        logger.debug("fetchGroupPasswd decode after: passwd: {}", passwd)
        return passwd
    }

    private fun getPasswdById(id: Int): Passwd? {
        return localDataSource.passwdsMap.flatMap { it.value }.find { passwd -> passwd.id == id }
    }

    private suspend fun updateGroupPasswdsByGroupId(
        groupId: Int,
        onUpdated: (() -> Unit)? = null,
        convert: (MutableList<Passwd>) -> MutableList<Passwd> = { passwds -> passwds }
    ) {
        localDataSource.updateGroupPasswds(groupId, convert).let {
            onUpdated?.invoke()
        }
    }

    private suspend fun clearGroupAndGroupPasswds() {
        localDataSource.updateGroups(mutableListOf())
        localDataSource.updateGroupPasswds(arrayListOf())
    }

    companion object {
        private const val TAG = "PasswdRepository"
        private const val PATTERN_PREFIX = "^.*(?i)"
        private const val PATTERN_SUFFIX = ".*"
    }
}