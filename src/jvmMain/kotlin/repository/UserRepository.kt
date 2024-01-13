package repository

import datasource.passwd.PasswdLocalDataSource
import datasource.user.UserRemoteDataSource
import entity.LoginResult
import entity.SignupResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import network.HttpClientObj
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object UserRepository {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource
    private val passwdLocalDataSource: PasswdLocalDataSource = PasswdLocalDataSource

    private val _loginResultFlow = MutableStateFlow<Result<LoginResult>?>(null)
    val loginResultStateFlow = _loginResultFlow.asStateFlow()

    private val _signResultFlow = MutableStateFlow<Result<SignupResult?>?>(null)
    val signResultStateFlow = _signResultFlow.asStateFlow()



    fun loginFailure(e: Throwable) {
        _loginResultFlow.value = Result.failure(e)
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String,
        host: String,
        port: Int,
    ): Result<LoginResult> {
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            logger.warn("(loginByPassword) warn, $username, $password, $secretKey can not be empty")
            val result = Result.failure<LoginResult>(Throwable("username, password, secretKey can not be empty or blank"))
            _loginResultFlow.value = result
            return result
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            logger.warn("(loginByPassword) warn, ${e.message}")
            val result = Result.failure<LoginResult>(e)
            _loginResultFlow.value = result
            return result
        }

        return userRemoteDataSource.loginByPassword(
            username = username,
            password = password,
        ).onSuccess { loginResult ->
            passwdLocalDataSource.onLoginSuccess(loginResult.passwds, secretKey)
            val result = Result.success(loginResult)
            _loginResultFlow.value = result
        }.onFailure {
            val result = Result.failure<LoginResult>(it)
            _loginResultFlow.value = result
        }
    }

    suspend fun signup(
        username: String,
        password: String,
        host: String,
        port: Int,
    ): Result<SignupResult?> {
        if (username.isBlank() || password.isBlank()) {
            logger.warn("sign up username and password can not be empty")
            val result = Result.failure<SignupResult>(Throwable("sign up username and password can not be empty"))
            _signResultFlow.value = result
            return result
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            logger.warn("(signup) forceUpdateHttpClient error, ${e.message}")
            val result = Result.failure<SignupResult>(e)
            _signResultFlow.value = result
            return result
        }

        return userRemoteDataSource.signup(
            username = username,
            password = password
        ).onSuccess { signupResult ->
            passwdLocalDataSource.onSignupSuccess()
            val result = Result.success(signupResult)
            _signResultFlow.value = result
        }.onFailure {
            val result = Result.failure<SignupResult>(it)
            _signResultFlow.value = result
        }
    }
}