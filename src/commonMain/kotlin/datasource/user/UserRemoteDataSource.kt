package datasource.user

import entity.LoginResult
import entity.SignupResult
import network.Apis
import network.KtorRequest
import network.entity.Param
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object UserRemoteDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

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

    suspend fun signup(
        username: String,
        password: String,
    ): Result<SignupResult> = KtorRequest.postRequest(
        needToken = false,
        needUserId = false,
        api = Apis.API_SIGNUP,
        params = listOf(
            Param("username", username),
            Param("password", password),
        )
    )
}