package model.viewmodel

import database.DataBase
import database.entity.HistoryData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import model.action.LoginAction
import model.uistate.LoginUiState
import network.WebSocketSyncUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.UserRepository

class UserViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val dataBase = DataBase.instance

    private val userRepository: UserRepository = UserRepository

    private val _loginUiState: MutableStateFlow<LoginUiState> by lazy {
        MutableStateFlow(LoginUiState(HistoryData.defaultHistoryData(), dataBase.getSavedHistories()))
    }
    val loginUiState: StateFlow<LoginUiState> by lazy {
        _loginUiState
    }

    init {
        silentlyLogin()
    }

    fun onAction(action: LoginAction) {
        logger.info("onAction: {}", action)
        with(action) {
            when (this) {
                is LoginAction.Login -> launch {
                    delay(200)
                    loginByPassword(
                        username = username,
                        password = password,
                        secretKey = secretKey,
                        host = host,
                        port = port,
                        saved = saved,
                        silentlyLogin = silentlyLogin
                    )
                }

                is LoginAction.Signup -> launch {
                    signup(
                        username = username,
                        password = password,
                        host = host,
                        port = port
                    )
                }

            }
        }
    }

    private fun silentlyLogin() = launch {
        val savedHistoryData = dataBase.latestSavedLoginHistoryData()
        if (savedHistoryData == null) {
            userRepository.loginFailure(Throwable())
            return@launch
        }
        if (savedHistoryData.silentlyLogin) {
            loginByPassword(
                username = savedHistoryData.username,
                password = savedHistoryData.password,
                secretKey = savedHistoryData.secretKey,
                host = savedHistoryData.host,
                port = savedHistoryData.port,
                saved = savedHistoryData.saved,
                silentlyLogin = true,
            )
            updateLoginUiState {
                copy(historyData = savedHistoryData)
            }
        } else {
            if (savedHistoryData.saved) {
                updateLoginUiState {
                    copy(historyData = savedHistoryData)
                }
            }
            userRepository.loginFailure(Throwable())
        }
    }

    private suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String,
        host: String,
        port: Int,
        saved: Boolean,
        silentlyLogin: Boolean,
    ) {
        userRepository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey,
            host = host,
            port = port,
        ).onSuccess {
            logger.info("(loginByPassword) success")
            updateDB(it.userId, username, password, secretKey, host, port, it.token, saved, silentlyLogin)
            WebSocketSyncUtil.startWebSocketListener(host, port, it.userId)
        }.onFailure {
            logger.error("(loginByPassword) error", it)
        }
    }

    private suspend fun signup(
        username: String,
        password: String,
        host: String,
        port: Int,
    ) {
        userRepository.signup(username, password, host, port)
            .onSuccess {
                if (it == null) {
                    return@onSuccess
                }
                coroutineScope {
                    withContext(Dispatchers.IO) {
                        dataBase.globalSecretKey.emit(it.secretKey)
                        dataBase.globalUserId.emit(it.userId)
                        dataBase.globalUsername.emit(username)
                        dataBase.globalAccessToken.emit(it.token)
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
    }

    private suspend fun updateDB(
        userId: Int,
        username: String,
        password: String,
        secretKey: String,
        host: String,
        port: Int,
        accessToken: String,
        saved: Boolean,
        silentlyLogin: Boolean
    ) {
        logger.info("(updateDB). username: $username, password: $password, secretKey: $secretKey, saved: $saved")
        val insertHistoryData = HistoryData(
            username = username,
            password = if (saved) password else "",
            secretKey = secretKey,
            host = host,
            port = port,
            accessToken = accessToken,
            saved = saved,
            silentlyLogin = silentlyLogin
        )
        val insertResultId = dataBase.insert(insertHistoryData)

        val historyData = if (insertResultId == -1) {
            HistoryData.defaultHistoryData()
        } else {
            insertHistoryData
        }

        withContext(Dispatchers.IO) {
            dataBase.globalSecretKey.emit(secretKey)
            dataBase.globalUserId.emit(userId)
            dataBase.globalUsername.emit(username)
            dataBase.globalAccessToken.emit(accessToken)
        }

        updateLoginUiState {
            copy(
                historyData = historyData,
                historyDataList = dataBase.getSavedHistories()
            )
        }
        logger.info("(saveLoginInfo) insertResultId: $insertResultId")
    }

    private fun updateLoginUiState(update: LoginUiState.() -> LoginUiState) {
        _loginUiState.update { update(it) }
    }
}