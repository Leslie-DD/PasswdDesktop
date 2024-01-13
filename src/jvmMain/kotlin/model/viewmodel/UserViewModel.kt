package model.viewmodel

import database.entity.HistoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import model.action.LoginAction
import model.uistate.LoginUiState
import network.WebSocketSyncUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.UserRepository

class UserViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val userRepository: UserRepository by lazy { UserRepository }

    private val _loginUiState: MutableStateFlow<LoginUiState> by lazy {
        MutableStateFlow(LoginUiState(HistoryData.defaultHistoryData(), userRepository.savedHistories))
    }
    val loginUiState: StateFlow<LoginUiState> by lazy { _loginUiState }

    init {
        silentlyLogin()
        collectHistoryData()
    }

    private fun collectHistoryData() = launch {
        userRepository.historyDataFlow.filterNotNull().collectLatest {
            updateLoginUiState { copy(historyData = it, historyDataList = userRepository.savedHistories) }
        }
    }

    fun onAction(action: LoginAction) {
        logger.info("onAction: {}", action)
        with(action) {
            when (this) {
                is LoginAction.Login -> launch {
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

    private fun silentlyLogin() = launch(Dispatchers.IO) {
        val savedHistoryData = userRepository.latestSavedLoginHistoryData
        if (savedHistoryData == null) {
            userRepository.loginFailure(Throwable())
            return@launch
        }
        if (savedHistoryData.saved) {
            updateLoginUiState { copy(historyData = savedHistoryData) }
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
        } else {
            userRepository.loginFailure(Throwable(message = null))
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
            saved = saved,
            silentlyLogin = silentlyLogin
        ).onSuccess {
            logger.info("(loginByPassword) success")
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
                logger.info("(signup) success")
                WebSocketSyncUtil.startWebSocketListener(host, port, it.userId)
            }.onFailure {
                logger.error("(signup) error", it)
            }
    }

    private fun updateLoginUiState(update: LoginUiState.() -> LoginUiState) {
        _loginUiState.update { update(it) }
    }
}