package passwds.repository

import network.KtorRequest
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult

class PasswdRepository {
    suspend fun fetchPasswds(): Result<List<Passwd>> = KtorRequest.postPasswds()
    suspend fun fetchGroups(): Result<List<Group>> = KtorRequest.postGroups()
    suspend fun fetchGroupPasswds(groupId: Int): Result<List<Passwd>> = KtorRequest.postGroupPasswds(groupId)

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
}