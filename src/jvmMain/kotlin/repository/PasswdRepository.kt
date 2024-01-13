package repository

import datasource.user.UserMemoryDataSource
import datasource.passwd.PasswdMemoryDataSource
import datasource.passwd.PasswdRemoteDataSource
import entity.Group
import entity.Passwd
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.AESUtil
import java.util.*

object PasswdRepository {

    private const val PATTERN_PREFIX = "^.*(?i)"
    private const val PATTERN_SUFFIX = ".*"

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val passwdRemoteDataSource: PasswdRemoteDataSource = PasswdRemoteDataSource
    private val passwdMemoryDataSource: PasswdMemoryDataSource = PasswdMemoryDataSource


    val groupsFlow: StateFlow<MutableList<Group>>
        get() = passwdMemoryDataSource.groups.asStateFlow()

    val groupPasswdsFlow: StateFlow<MutableList<Passwd>>
        get() = passwdMemoryDataSource.groupPasswds.asStateFlow()


    suspend fun refreshGroupPasswds(groupId: Int) {
        passwdMemoryDataSource.emitGroupPasswds(groupId)
    }

    suspend fun fetchGroups() = passwdRemoteDataSource.fetchGroups()
        .onSuccess { remoteGroups ->
            passwdMemoryDataSource.emitGroups(remoteGroups)
            if (remoteGroups.isNotEmpty()) {
                passwdMemoryDataSource.emitGroupPasswds(remoteGroups.first().id)
            }
        }.onFailure {
            clearGroupAndGroupPasswds()
            logger.error("(fetchGroups) error", it)
        }

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Group> {
        var result = Result.failure<Group>(Throwable())
        passwdRemoteDataSource.newGroup(
            groupName = groupName,
            groupComment = groupComment
        ).onSuccess {
            val newGroup = Group(
                id = it,
                userId = UserMemoryDataSource.globalUserId.value,
                groupName = groupName,
                groupComment = groupComment
            )
            passwdMemoryDataSource.newGroup(newGroup)
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
        passwdRemoteDataSource.deleteGroup(
            groupId = groupId
        ).onSuccess {
            val deleteGroup = passwdMemoryDataSource.deleteGroup(groupId)
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
        passwdRemoteDataSource.updateGroup(
            groupId = groupId,
            groupName = groupName,
            groupComment = groupComment
        ).onSuccess {
            val updateGroup = passwdMemoryDataSource.updateGroup(groupId, groupName, groupComment)
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
        val secretKeyByteArray = Base64.getDecoder().decode(UserMemoryDataSource.globalSecretKey.value)
        var result = Result.failure<Passwd>(Throwable())
        passwdRemoteDataSource.newPasswd(
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
                userId = UserMemoryDataSource.globalUserId.value,
                link = link,
                comment = comment,
                usernameString = usernameString,
                passwordString = passwordString
            )
            passwdMemoryDataSource.newPasswd(newPasswd)
            result = Result.success(newPasswd)
        }.onFailure {
            result = Result.failure(it)
        }
        return result
    }

    suspend fun deletePasswd(id: Int): Result<Passwd> {
        var result = Result.failure<Passwd>(Throwable())
        passwdRemoteDataSource.deletePasswd(id = id)
            .onSuccess {
                val deletePasswd = passwdMemoryDataSource.deletePasswd(id)
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
        val secretKeyByteArray = Base64.getDecoder().decode(UserMemoryDataSource.globalSecretKey.value)
        var result = Result.failure<Passwd>(Throwable())
        passwdRemoteDataSource.updatePasswd(
            id = id,
            title = AESUtil.encrypt(secretKeyByteArray, title),
            usernameString = AESUtil.encrypt(secretKeyByteArray, usernameString),
            passwordString = AESUtil.encrypt(secretKeyByteArray, passwordString),
            link = link,
            comment = comment
        ).onSuccess {
            val updatePasswd = passwdMemoryDataSource.updatePasswd(
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
            passwdMemoryDataSource.emitGroupPasswds(arrayListOf())
        } else {
            val pattern = Regex(PATTERN_PREFIX + value + PATTERN_SUFFIX)
            val result = arrayListOf<Passwd>()
            passwdMemoryDataSource.passwdsMap
                .flatMap { it.value }
                .forEach { passwd ->
                    if (passwd.title?.matches(pattern) == true || passwd.usernameString?.matches(pattern) == true) {
                        result.add(passwd)
                    }
                }
            passwdMemoryDataSource.emitGroupPasswds(result)
        }
    }

    fun getAllGroupsWithPasswds(): MutableMap<String, MutableList<Passwd>> {
        val passwdsMap = passwdMemoryDataSource.passwdsMap
        val resultMap: MutableMap<String, MutableList<Passwd>> = mutableMapOf()
        passwdMemoryDataSource.groups.value.forEach { group ->
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
        passwdMemoryDataSource.emitGroups(arrayListOf())
        passwdMemoryDataSource.emitGroupPasswds(arrayListOf())
    }

}