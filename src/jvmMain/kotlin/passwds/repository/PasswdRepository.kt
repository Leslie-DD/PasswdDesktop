package passwds.repository

import network.KtorRequest
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult

class PasswdRepository {
    suspend fun fetchPasswds(): Result<List<Passwd>> = KtorRequest.postPasswds()
    suspend fun fetchGroups(): Result<MutableList<Group>> = KtorRequest.postGroups()
    suspend fun fetchGroupPasswds(groupId: Int): Result<MutableList<Passwd>> = KtorRequest.postGroupPasswds(groupId)

    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> = KtorRequest.loginByToken(
        username = username,
        token = token,
        secretKey = secretKey
    )

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> = KtorRequest.loginByPassword(
        username = username,
        password = password,
        secretKey = secretKey
    )

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult> = KtorRequest.register(
        username = username,
        password = password,
    )

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Int> = KtorRequest.newGroup(
        groupName = groupName,
        groupComment = groupComment,
    )

    suspend fun deleteGroup(groupId: Int): Result<Int> = KtorRequest.deleteGroup(groupId)

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Result<Int> = KtorRequest.updateGroup(
        groupId = groupId,
        groupName = groupName,
        groupComment = groupComment
    )

    suspend fun newPasswd(
        groupId: Int,
        title: String,
        username: String,
        password: String,
        link: String,
        comment: String,
    ): Result<Int> = KtorRequest.newPasswd(
        groupId = groupId,
        title = title,
        username = username,
        password = password,
        link = link,
        comment = comment
    )

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameStr: String?,
        passwordStr: String?,
        link: String?,
        comment: String?
    ): Result<Int> = KtorRequest.updatePasswd(
        id = id,
        title = title,
        usernameStr = usernameStr,
        passwordStr = passwordStr,
        link = link,
        comment = comment
    )

    suspend fun deletePasswd(passwdId: Int): Result<Int> = KtorRequest.deletePasswd(passwdId)
}