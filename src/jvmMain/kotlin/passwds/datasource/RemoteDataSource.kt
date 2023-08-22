package passwds.datasource

import network.Api
import network.KtorRequest
import network.Param
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult

class RemoteDataSource {

    suspend fun fetchPasswds(): Result<MutableList<Passwd>> = KtorRequest.postRequest(
        api = Api.API_PASSWDS
    )

    suspend fun fetchGroups(): Result<MutableList<Group>> = KtorRequest.postRequest(
        api = Api.API_GROUPS
    )

    suspend fun fetchGroupPasswds(
        groupId: Int
    ): Result<MutableList<Passwd>> = KtorRequest.postRequest(
        api = Api.API_GROUP_PASSWDS,
        params = listOf(
            Param("group_id", groupId),
        )
    )


    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        needSecretKey = false,
        api = Api.API_LOGIN_BY_TOKEN,
        params = listOf(
            Param("username", username),
            Param("token", token),
            Param("secret_key", secretKey)
        )
    )

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        needSecretKey = false,
        api = Api.API_LOGIN_BY_PASSWORD,
        params = listOf(
            Param("username", username),
            Param("password", password),
            Param("secret_key", secretKey)
        )
    )

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        needSecretKey = false,
        api = Api.API_REGISTER,
        params = listOf(
            Param("username", username),
            Param("password", password),
        )
    )

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Int> = KtorRequest.postRequest(
        needSecretKey = false,
        api = Api.API_NEW_GROUP,
        params = listOf(
            Param("group_name", groupName),
            Param("group_comment", groupComment)
        )
    )

    suspend fun deleteGroup(groupId: Int): Result<Int> = KtorRequest.postRequest(
        api = Api.API_DELETE_GROUP,
        needSecretKey = false,
        params = listOf(
            Param("group_id", groupId)
        )
    )

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Result<Int> = KtorRequest.postRequest(
        api = Api.API_UPDATE_GROUP,
        needSecretKey = false,
        params = listOf(
            Param("id", groupId),
            Param("group_name", groupName),
            Param("group_comment", groupComment)
        )
    )

    suspend fun newPasswd(
        groupId: Int,
        title: String,
        username: String,
        password: String,
        link: String,
        comment: String,
    ): Result<Int> = KtorRequest.postRequest(
        api = Api.API_NEW_PASSWD,
        params = listOf(
            Param("group_id", groupId),
            Param("title", title),
            Param("username", username),
            Param("password", password),
            Param("link", link),
            Param("comment", comment)
        )
    )

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameStr: String?,
        passwordStr: String?,
        link: String?,
        comment: String?
    ): Result<Int> = KtorRequest.postRequest(
        api = Api.API_UPDATE_PASSWD,
        params = listOf(
            Param("id", id),
            Param("title", title ?: ""),
            Param("username_string", usernameStr ?: ""),
            Param("password_string", passwordStr ?: ""),
            Param("link", link ?: ""),
            Param("comment", comment ?: "")
        )
    )

    suspend fun deletePasswd(
        id: Int
    ): Result<Int> = KtorRequest.postRequest(
        api = Api.API_DELETE_PASSWD,
        needSecretKey = false,
        params = listOf(
            Param("id", id)
        )
    )

}