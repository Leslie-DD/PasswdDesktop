package passwds.model

import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import config.LocalPref
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import model.Setting
import org.jetbrains.skia.impl.Log
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.repository.PasswdRepository
import platform.desktop.Platform
import platform.desktop.currentPlatform

class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val repository: PasswdRepository = PasswdRepository()

    val scaffoldState = ScaffoldState(DrawerState(DrawerValue.Closed), SnackbarHostState())

    val shouldBeLandscape = MutableStateFlow(currentPlatform == Platform.Desktop)

    private val _uiState = MutableStateFlow(TranslateUiState.Default)
    val translateUiState get() = _uiState.value
    val uiState: TranslateUiState @Composable get() = _uiState.collectAsState().value

    val exitApp = MutableStateFlow(false)

    val theme = MutableStateFlow(LocalPref.theme)

    val settings = Setting()

    init {
        launch(Dispatchers.IO) {
            loginByToken()
        }
    }

    private fun clearData() {
        updateUiState {
            copy(
                passwds = emptyList(),
                groups = arrayListOf(),
                groupPasswds = emptyList(),
                selectGroup = null,
                selectPasswd = null
            )
        }
    }

    private suspend fun loginByToken() {
        Log.error("PasswdsViewModel().loginByToken start")
        val username = LocalPref.username
        if (username.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val token = LocalPref.accessToken
        if (token.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val secretKey = LocalPref.secretKey
        if (secretKey.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        Log.error("PasswdsViewModel().loginByToken start, token: $token")
        repository.loginByToken(
            username = username,
            token = token,
            secretKey = secretKey
        ).onSuccess {
            Log.error("PasswdsViewModel().loginByToken success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            Log.error("PasswdsViewModel().loginByToken error:${it.message}")
            it.printStackTrace()
            updateUiState { copy(uiScreen = UiScreen.Login) }
        }
    }

    private suspend fun onLoginSuccess(secretKey: String, loginResult: LoginResult) {
        updateUiState { copy(uiScreen = UiScreen.Passwds) }
        coroutineScope {
            withContext(Dispatchers.IO) {
                settings.secretKey.emit(secretKey)
                settings.userId.emit(loginResult.user_id)
                settings.username.emit(loginResult.username)
                settings.accessToken.emit(loginResult.token)
                clearData()
            }
            launch(Dispatchers.IO) {
                fetchPasswds()
            }
            launch(Dispatchers.IO) {
                fetchGroups()
            }
        }
    }

    private suspend fun register(
        username: String,
        password: String,
    ) {
        Log.error("PasswdsViewModel().register start")
        if (username.isBlank() || password.isBlank()) {
            Log.error("PasswdsViewModel().register error, $username, $password")
            return
        }
        repository.register(
            username = username,
            password = password,
        ).onSuccess {
            Log.error("PasswdsViewModel().register success, result: $it")
            updateUiState { copy(uiScreen = UiScreen.Passwds) }
            coroutineScope {
                withContext(Dispatchers.IO) {
                    settings.secretKey.emit(it.secret_key)
                    settings.userId.emit(it.user_id)
                    settings.username.emit(username)
                    settings.accessToken.emit(it.token)
                    clearData()
                }

                launch(Dispatchers.IO) {
                    fetchPasswds()
                }
                launch(Dispatchers.IO) {
                    fetchGroups()
                }
            }
        }.onFailure {
            Log.error("PasswdsViewModel().register error: ${it.message}")
            it.printStackTrace()
        }
    }

    private suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ) {
        Log.error("PasswdsViewModel().loginByPassword start")
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            Log.error("PasswdsViewModel().loginByPassword error, $username, $password, $secretKey")
            return
        }
        repository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess {
            Log.error("PasswdsViewModel().loginByPassword success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            Log.error("PasswdsViewModel().loginByPassword error: ${it.message}")
            it.printStackTrace()
        }
    }

    private suspend fun fetchPasswds() {
        Log.error("PasswdsViewModel().fetchPasswds start")
        repository.fetchPasswds()
            .onSuccess {
                Log.error("PasswdsViewModel().fetchPasswds success, size:${it.size}")
                updateUiState {
                    copy(
                        passwds = it
                    )
                }
            }.onFailure {
                Log.error("PasswdsViewModel().fetchPasswds error:${it.message}")
                it.printStackTrace()
            }
    }

    private suspend fun fetchGroups() {
        Log.error("PasswdsViewModel().fetchGroups start")
        repository.fetchGroups()
            .onSuccess {
                Log.error("PasswdsViewModel().fetchGroups success, size:${it.size}")
                updateUiState {
                    copy(
                        groups = it
                    )
                }
            }.onFailure {
                Log.error("PasswdsViewModel().fetchGroups error:${it.message}")
                it.printStackTrace()
            }
    }

    private suspend fun fetchGroupPasswds(groupId: Int) = withContext(Dispatchers.IO) {
        Log.error("PasswdsViewModel().fetchGroupPasswds start")
        repository.fetchGroupPasswds(groupId)
            .onSuccess {
                Log.error("PasswdsViewModel().fetchGroupPasswds success, size:${it.size}")
                updateUiState {
                    copy(
                        groupPasswds = it
                    )
                }
            }.onFailure {
                Log.error("PasswdsViewModel().fetchGroupPasswds error:${it.message}")
                updateUiState {
                    copy(
                        groupPasswds = emptyList()
                    )
                }
                it.printStackTrace()
            }
    }

    private suspend fun newGroup(
        groupName: String,
        groupComment: String
    ) {
        if (groupName.isBlank()) {
            // TODO: tips 提示 groupName 不能为空
            return
        }
        repository.newGroup(groupName, groupComment)
            .onSuccess {
                val newGroup = Group(
                    id = it,
                    userId = settings.userId.value,
                    groupName = groupName,
                    groupComment = groupComment
                )
                updateUiState {
                    copy(
                        groups = groups.apply { add(newGroup) },
                        effect = UiEffect.NewGroupResult(newGroup),
                        selectGroup = newGroup,
                        groupPasswds = emptyList()
                    )
                }
            }.onFailure {
                updateUiState {
                    copy(
                        effect = UiEffect.NewGroupResult(null)
                    )
                }
            }
    }

    private suspend fun deleteGroup(groupId: Int) {
        repository.deleteGroup(groupId)
            .onSuccess {
                val deleteGroup = getGroup(groupId)
                updateUiState {
                    copy(
                        groups = groups.apply {
                            remove(deleteGroup)
                        },
                        effect = UiEffect.DeleteGroupResult(deleteGroup)
                    )
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateUiState {
                    copy(
                        effect = UiEffect.DeleteGroupResult(null)
                    )
                }
            }
    }

    fun onAction(action: UiAction) {
        with(action) {
            when (this) {
                is UiAction.GoScreen -> {
                    updateUiState { copy(uiScreen = screen) }
                }

                is UiAction.ExitApp -> {
                    exitApp.tryEmit(true)
                }

                is UiAction.WindowVisible -> {
                    updateUiState {
                        copy(windowVisible = visible)
                    }
                }

                is UiAction.ShowGroupPasswds -> {
                    updateUiState {
                        copy(
                            selectGroup = getGroup(groupId),
                            selectPasswd = null
                        )
                    }
                    launch(Dispatchers.IO) {
                        fetchGroupPasswds(groupId)
                    }
                }

                is UiAction.ShowPasswd -> {
                    updateUiState {
                        copy(
                            selectPasswd = getPasswd(passwdId)
                        )
                    }
                }

                is UiAction.Login -> {
                    launch(Dispatchers.IO) {
                        loginByPassword(
                            username = username,
                            password = password,
                            secretKey = secretKey
                        )
                    }
                }

                is UiAction.Register -> {
                    launch(Dispatchers.IO) {
                        register(
                            username = username,
                            password = password
                        )
                    }
                }

                is UiAction.NewGroup -> {
                    launch(Dispatchers.IO) {
                        newGroup(
                            groupName = groupName,
                            groupComment = groupComment
                        )
                    }
                }

                is UiAction.DeleteGroup -> {
                    val selectGroupId = translateUiState.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        deleteGroup(selectGroupId)
                    }
                }

                is UiAction.ClearEffect -> {
                    updateUiState {
                        copy(
                            effect = null
                        )
                    }
                }

                else -> {}
            }
        }
    }

    private fun getPasswd(passwdId: Int): Passwd? =
        translateUiState.passwds.find { passwd: Passwd -> passwd.id == passwdId }

    private fun getGroup(groupId: Int): Group? = translateUiState.groups.find { group: Group -> group.id == groupId }


    /**
     * 更新页面状态
     * 调用时在函数块中用 data class 的 copy函数就行
     */
    fun updateUiState(update: TranslateUiState.() -> TranslateUiState) {
        _uiState.update { update(it) }
    }

    init {
        launch {
            shouldBeLandscape.stateIn(this, SharingStarted.WhileSubscribed(), TranslateUiState.Default.isLandscape)
                .collectLatest {
                    updateUiState { copy(isLandscape = it) }
                }
        }
    }

}