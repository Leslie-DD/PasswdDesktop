package repository

import database.DataBase
import datasource.LocalDataSource
import datasource.RemoteDataSource
import entity.Group
import entity.LoginResult
import entity.Passwd
import entity.SignupResult
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
            if (passwdsMapResult[passwd.groupId] == null) {
                passwdsMapResult[passwd.groupId] = arrayListOf()
            }
            passwdsMapResult[passwd.groupId]?.add(decodePasswd(secretKeyByteArray, passwd))
        }
        return passwdsMapResult
    }

    suspend fun refreshGroupPasswds(groupId: Int) {
        localDataSource.emitGroupPasswds(groupId)
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
            .onSuccess { remoteGroups ->
                localDataSource.emitGroups(remoteGroups)
                if (remoteGroups.isNotEmpty()) {
                    localDataSource.emitGroupPasswds(remoteGroups.first().id)
                }
            }.onFailure {
                clearGroupAndGroupPasswds()
                logger.error("(fetchGroups) error", it)
            }
    }

    suspend fun signup(
        username: String,
        password: String,
    ): Result<SignupResult?> {
        return remoteDataSource.signup(
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
            localDataSource.newGroup(newGroup)
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
            val deleteGroup = localDataSource.deleteGroup(groupId)
            result = if (deleteGroup == null) {
                Result.failure(Throwable("no such group"))
            } else {
                Result.success(deleteGroup)
            }
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
            val updateGroup = localDataSource.updateGroup(groupId, groupName, groupComment)
            result = if (updateGroup == null) {
                Result.failure(Throwable("no such group"))
            } else {
                Result.success(updateGroup)
            }
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
            localDataSource.newPasswd(newPasswd)
            result = Result.success(newPasswd)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun deletePasswd(id: Int): Result<Passwd> {
        var result = Result.failure<Passwd>(Throwable())
        remoteDataSource.deletePasswd(id = id)
            .onSuccess {
                val deletePasswd = localDataSource.deletePasswd(id)
                result = if (deletePasswd == null) {
                    Result.failure(Throwable("no such passwd"))
                } else {
                    Result.success(deletePasswd)
                }
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
            val updatePasswd = localDataSource.updatePasswd(
                id = id,
                title = title,
                usernameString = usernameString,
                passwordString = passwordString,
                link = link,
                comment = comment
            )
            result = if (updatePasswd == null) {
                Result.failure(Throwable("no such passwd"))
            } else {
                Result.success(updatePasswd)
            }
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun searchLikePasswdsAndUpdate(value: String) {
        if (value.isBlank()) {
            localDataSource.emitGroupPasswds(arrayListOf())
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
            localDataSource.emitGroupPasswds(result)
        }
    }

    private fun decodePasswd(
        secretKeyBytes: ByteArray? = null,
        passwd: Passwd
    ): Passwd {
        return try {
            passwd.copy(
                title = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.title),
                usernameString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.usernameString),
                passwordString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.passwordString)
            )
        } catch (e: Exception) {
            logger.error("(decodePasswd) error ", e)
            passwd
        }
    }

    fun getAllGroupsWithPasswds(): MutableMap<String, MutableList<Passwd>> {
        val passwdsMap = localDataSource.passwdsMap
        val resultMap: MutableMap<String, MutableList<Passwd>> = mutableMapOf()
        localDataSource.groups.value.forEach { group ->
            passwdsMap[group.id]?.let {
                val keyString: String = if (group.groupName.isNullOrEmpty()) {
                    if (group.groupComment.isNullOrEmpty()) {
                        group.id.toString()
                    } else {
                        group.groupComment!!
                    }
                } else {
                    group.groupName!!
                }
                resultMap[keyString] = it
            }
        }
        return resultMap
    }

    private suspend fun clearGroupAndGroupPasswds() {
        localDataSource.emitGroups(arrayListOf())
        localDataSource.emitGroupPasswds(arrayListOf())
    }

    companion object {
        private const val PATTERN_PREFIX = "^.*(?i)"
        private const val PATTERN_SUFFIX = ".*"
    }
}