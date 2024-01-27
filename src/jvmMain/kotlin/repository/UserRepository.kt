package repository

import datasource.user.UserMemoryDataSource
import datasource.DatabaseDataSource
import datasource.passwd.PasswdMemoryDataSource
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
    private val userMemoryDataSource: UserMemoryDataSource = UserMemoryDataSource
    private val passwdMemoryDataSource: PasswdMemoryDataSource = PasswdMemoryDataSource
    private val databaseDataSource: DatabaseDataSource = DatabaseDataSource

    private val _loginResultFlow: MutableStateFlow<Result<LoginResult>?> by lazy { MutableStateFlow(null) }
    val loginResultStateFlow by lazy { _loginResultFlow.asStateFlow() }

    private val _signResultFlow = MutableStateFlow<Result<SignupResult?>?>(null)
    val signResultStateFlow = _signResultFlow.asStateFlow()

    val userDataFlow
        get() = databaseDataSource.userData

    val savedHistories
        get() = databaseDataSource.savedUsers

    val latestSavedUserData
        get() = databaseDataSource.latestSavedUserData


    fun loginFailure(e: Throwable) {
        _loginResultFlow.value = Result.failure(e)
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String,
        host: String,
        port: Int,
        saved: Boolean,
        silentlyLogin: Boolean,
    ): Result<LoginResult> {
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            val result = Result.failure<LoginResult>(Throwable("username, password, secretKey can not be empty or blank"))
            _loginResultFlow.value = result
            return result
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            val result = Result.failure<LoginResult>(e)
            _loginResultFlow.value = result
            return result
        }

        return userRemoteDataSource.loginByPassword(
            username = username,
            password = password,
        ).onSuccess { loginResult ->
            passwdMemoryDataSource.onLoginSuccess(loginResult.passwds, secretKey)
            databaseDataSource.insertHistoryData(username, password, secretKey, host, port, loginResult.token, saved, silentlyLogin)
            userMemoryDataSource.updateGlobalValues(
                secretKey = secretKey,
                userId = loginResult.userId,
                username = username,
                token = loginResult.token
            )
            val result = Result.success(loginResult)
            _loginResultFlow.value = result
            HttpClientObj.startWebSocketListener(host, port, loginResult.userId)
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
    ): Result<SignupResult> {
        if (username.isBlank() || password.isBlank()) {
            val result = Result.failure<SignupResult>(Throwable("sign up username and password can not be empty"))
            _signResultFlow.value = result
            return result
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            val result = Result.failure<SignupResult>(e)
            _signResultFlow.value = result
            return result
        }

        return userRemoteDataSource.signup(
            username = username,
            password = password
        ).onSuccess { signupResult ->
            passwdMemoryDataSource.onSignupSuccess()
            userMemoryDataSource.updateGlobalValues(
                secretKey = signupResult.secretKey,
                userId = signupResult.userId,
                username = username,
                token = signupResult.token
            )
            val result = Result.success(signupResult)
            _signResultFlow.value = result
            HttpClientObj.startWebSocketListener(host, port, signupResult.userId)
        }.onFailure {
            val result = Result.failure<SignupResult>(it)
            _signResultFlow.value = result
        }
    }
}