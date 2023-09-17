package datasource

import entity.Group
import entity.LoginResult
import entity.Passwd
import entity.RegisterResult
import network.Apis
import network.KtorRequest
import network.entity.Param

class RemoteDataSource {

    suspend fun fetchPasswds(): Result<MutableList<Passwd>> = KtorRequest.postRequest(
        api = Apis.API_PASSWDS
    )

    suspend fun fetchGroupPasswds(
        groupId: Int
    ): Result<MutableList<Passwd>> = KtorRequest.postRequest(
        api = Apis.API_GROUP_PASSWDS,
        params = listOf(
            Param("group_id", groupId),
        )
    )

    suspend fun fetchGroups(): Result<MutableList<Group>> = KtorRequest.postRequest(
        api = Apis.API_GROUPS
    )

    suspend fun loginByToken(
        username: String,
        token: String
    ): Result<LoginResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        api = Apis.API_LOGIN_BY_TOKEN,
        params = listOf(
            Param("username", username),
            Param("token", token),
        )
    )

    suspend fun loginByPassword(
        username: String,
        password: String,
    ): Result<LoginResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        api = Apis.API_LOGIN_BY_PASSWORD,
        params = listOf(
            Param("username", username),
            Param("password", password),
        )
    )

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult?> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        api = Apis.API_REGISTER,
        params = listOf(
            Param("username", username),
            Param("password", password),
        )
    )

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_NEW_GROUP,
        params = listOf(
            Param("group_name", groupName),
            Param("group_comment", groupComment)
        )
    )

    suspend fun deleteGroup(groupId: Int): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_DELETE_GROUP,
        params = listOf(
            Param("group_id", groupId)
        )
    )

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_UPDATE_GROUP,
        params = listOf(
            Param("id", groupId),
            Param("group_name", groupName),
            Param("group_comment", groupComment)
        )
    )

    suspend fun newPasswd(
        groupId: Int,
        title: String,
        usernameString: String?,
        passwordString: String?,
        link: String,
        comment: String,
    ): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_NEW_PASSWD,
        params = listOf(
            Param("group_id", groupId),
            Param("title", title),
            Param("username_string", usernameString ?: ""),
            Param("password_string", passwordString ?: ""),
            Param("link", link),
            Param("comment", comment)
        )
    )

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameString: String?,
        passwordString: String?,
        link: String?,
        comment: String?
    ): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_UPDATE_PASSWD,
        params = listOf(
            Param("id", id),
            Param("title", title ?: ""),
            Param("username_string", usernameString ?: ""),
            Param("password_string", passwordString ?: ""),
            Param("link", link ?: ""),
            Param("comment", comment ?: "")
        )
    )

    suspend fun deletePasswd(
        id: Int
    ): Result<Int> = KtorRequest.postRequest(
        api = Apis.API_DELETE_PASSWD,
        params = listOf(
            Param("id", id)
        )
    )

}