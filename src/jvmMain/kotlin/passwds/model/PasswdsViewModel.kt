package passwds.model

import config.LocalPref
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import model.Setting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.model.uistate.DialogUiState
import passwds.model.uistate.GroupUiState
import passwds.model.uistate.PasswdUiState
import passwds.model.uistate.WindowUiState
import passwds.repository.PasswdRepository
import platform.desktop.Platform
import platform.desktop.currentPlatform

class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val remoteRepository: PasswdRepository = PasswdRepository()

    val shouldBeLandscape = MutableStateFlow(currentPlatform == Platform.Desktop)

    private val _windowUiState = MutableStateFlow(WindowUiState.Default)
    val windowUiState: StateFlow<WindowUiState> = _windowUiState

    private val _groupUiState: MutableStateFlow<GroupUiState> by lazy {
        MutableStateFlow(GroupUiState.defaultGroupUiState())
    }
    val groupUiState: StateFlow<GroupUiState> by lazy {
        _groupUiState
    }

    private val _passwdUiState: MutableStateFlow<PasswdUiState> by lazy {
        MutableStateFlow(PasswdUiState.defaultPasswdUiState())
    }
    val passwdUiState: StateFlow<PasswdUiState> by lazy {
        _passwdUiState
    }
    private val _dialogUiState: MutableStateFlow<DialogUiState> by lazy {
        MutableStateFlow(DialogUiState.defaultDialogUiState())
    }
    val dialogUiState: StateFlow<DialogUiState> by lazy {
        _dialogUiState
    }

    val exitApp = MutableStateFlow(false)

    val theme = MutableStateFlow(LocalPref.theme)

    val settings = Setting()

    init {
        launch(Dispatchers.IO) {
            loginByToken()
        }

        launch {
            remoteRepository.groups.collect {
                updateGroupUiState {
                    copy(groups = it)
                }
            }
        }

        launch {
            remoteRepository.groupPasswds.collect {
                updatePasswdUiState {
                    copy(groupPasswds = it)
                }
            }
        }
    }

    private fun clearData() {
        updatePasswdUiState {
            copy(selectPasswd = null)
        }
        updateGroupUiState {
            copy(selectGroup = null)
        }
    }

    private suspend fun loginByToken() {
        val username = LocalPref.username
        logger.debug("PasswdsViewModel().loginByToken, username: $username")
        if (username.isBlank()) {
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val token = LocalPref.accessToken
        logger.debug("PasswdsViewModel().loginByToken, token: $token")
        if (token.isBlank()) {
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val secretKey = LocalPref.secretKey
        logger.debug("PasswdsViewModel().loginByToken, secretKey: $secretKey")
        if (secretKey.isBlank()) {
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        remoteRepository.loginByToken(
            username = username,
            token = token,
            secretKey = secretKey
        ).onSuccess {
            logger.info("PasswdsViewModel().loginByToken success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            logger.error("PasswdsViewModel().loginByToken error:${it.message}")
            it.printStackTrace()
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
        }
    }

    private suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ) {
        logger.debug("PasswdsViewModel().loginByPassword start")
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            logger.error("PasswdsViewModel().loginByPassword error, $username, $password, $secretKey")
            return
        }
        remoteRepository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess {
            logger.info("PasswdsViewModel().loginByPassword success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            logger.error("PasswdsViewModel().loginByPassword error: ${it.message}")
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun onLoginSuccess(secretKey: String, loginResult: LoginResult) {
        updateWindowUiState { copy(uiScreen = UiScreen.Passwds) }
        coroutineScope {
            withContext(Dispatchers.IO) {
                settings.secretKey.emit(secretKey)
                settings.userId.emit(loginResult.userId)
                settings.username.emit(loginResult.username)
                settings.accessToken.emit(loginResult.token)
                clearData()
            }
            withContext(Dispatchers.IO) {
                fetchPasswds()
            }
            fetchGroups()
            updateDialogUiState {
                copy(effect = null)
            }
        }
    }

    private suspend fun register(
        username: String,
        password: String,
    ) {
        logger.debug("PasswdsViewModel().register start")
        if (username.isBlank() || password.isBlank()) {
            logger.error("PasswdsViewModel().register error, $username, $password")
            return
        }
        remoteRepository.register(
            username = username,
            password = password,
        ).onSuccess {
            logger.info("PasswdsViewModel().register success, result: $it")
            if (it == null) {
                return@onSuccess
            }
            updateWindowUiState { copy(uiScreen = UiScreen.Passwds) }
            coroutineScope {
                withContext(Dispatchers.IO) {
                    settings.secretKey.emit(it.secretKey)
                    settings.userId.emit(it.userId)
                    settings.username.emit(username)
                    settings.accessToken.emit(it.token)
                    clearData()
                }

                withContext(Dispatchers.IO) {
                    fetchPasswds()
                }
                fetchGroups()
            }
        }.onFailure {
            logger.error("PasswdsViewModel().register error: ${it.message}")
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun fetchPasswds() {
        logger.debug("PasswdsViewModel().fetchPasswds start")
        remoteRepository.fetchPasswds()
    }

    private suspend fun fetchGroups() {
        logger.debug("PasswdsViewModel().fetchGroups start")
        remoteRepository.fetchGroups()
    }

    private suspend fun fetchGroupPasswds(groupId: Int) {
        logger.debug("PasswdsViewModel().fetchGroupPasswds start")
        remoteRepository.fetchGroupPasswds(groupId)
    }

    private suspend fun newGroup(
        groupName: String,
        groupComment: String
    ) {
        if (groupName.isBlank()) {
            // TODO: tips 提示 groupName 不能为空
            return
        }
        remoteRepository.newGroup(groupName, groupComment)
            .onSuccess {
                updateDialogUiState {
                    copy(effect = DialogUiEffect.NewGroupResult(it))
                }
                updateGroupUiState {
                    copy(selectGroup = it,)
                }
                updatePasswdUiState {
                    copy(selectPasswd = null)
                }
            }.onFailure {
                updateDialogUiState {
                    copy(effect = DialogUiEffect.NewGroupResult(null))
                }
            }
    }

    private suspend fun deleteGroup(groupId: Int) {
        remoteRepository.deleteGroup(groupId)
            .onSuccess {
                updateGroupUiState {
                    copy(selectGroup = null,)
                }
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeleteGroupResult(it))
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeleteGroupResult(null))
                }
            }
    }

    private suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ) {
        remoteRepository.updateGroup(
            groupId = groupId,
            groupName = groupName,
            groupComment = groupComment
        ).onSuccess {
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdateGroupResult(it))
            }
        }.onFailure {
            // TODO: 删除失败的情况 tips 提示
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdateGroupResult(null))
            }
        }
    }

    private suspend fun newPasswd(
        groupId: Int,
        title: String,
        usernameString: String,
        passwordString: String,
        link: String,
        comment: String,
    ) {
        logger.debug("PasswdsViewModel().newPasswd start")
        remoteRepository.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = usernameString,
            passwordString = passwordString,
            link = link,
            comment = comment,
        ).onSuccess {
            logger.info("PasswdsViewModel().newPasswd onSuccess, $it")
            updateDialogUiState {
                copy(effect = DialogUiEffect.NewPasswdResult(it))
            }
            updatePasswdUiState {
                copy(selectPasswd = it)
            }
        }.onFailure {
            logger.error("PasswdsViewModel().newPasswd onFailure")
            it.printStackTrace()
            updateDialogUiState {
                copy(effect = DialogUiEffect.NewGroupResult(null))
            }
        }
    }


    private suspend fun updatePasswd(
        updatePasswd: Passwd
    ) {
        logger.debug("PasswdsViewModel().updatePasswd start")
        delay(1000)
        remoteRepository.updatePasswd(
            id = updatePasswd.id,
            title = updatePasswd.title,
            usernameStrValue = updatePasswd.usernameString,
            passwordStrValue = updatePasswd.passwordString,
            link = updatePasswd.link,
            comment = updatePasswd.comment
        ).onSuccess { passwd ->
            logger.info("PasswdsViewModel().updatePasswd onSuccess, count: $passwd")
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdatePasswdResult(passwd))
            }
            updatePasswdUiState {
                copy(selectPasswd = passwd)
            }
        }.onFailure {
            logger.error("PasswdsViewModel().updatePasswd onFailure")
            it.printStackTrace()
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdatePasswdResult(null))
            }
        }
    }

    private suspend fun deletePasswd(passwdId: Int) {
        remoteRepository.deletePasswd(passwdId)
            .onSuccess {
                val deletePasswd = getGroupPasswd(passwdId)
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeletePasswdResult(deletePasswd))
                }
                updatePasswdUiState {
                    copy(selectPasswd = null)
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeletePasswdResult(null))
                }
            }
    }

    fun onAction(action: UiAction) {
        logger.debug("onAction: $action")
        with(action) {
            when (this) {
                is UiAction.GoScreen -> {
                    updateWindowUiState {
                        copy(uiScreen = screen)
                    }
                }

                is UiAction.ExitApp -> {
                    exitApp.tryEmit(true)
                }

                is UiAction.WindowVisible -> {
                    updateWindowUiState {
                        copy(windowVisible = visible)
                    }
                }

                is UiAction.ShowGroupPasswds -> {
                    updatePasswdUiState {
                        copy(selectPasswd = null)
                    }
                    updateGroupUiState {
                        copy(selectGroup = getGroup(groupId))
                    }
                    launch(Dispatchers.IO) {
                        fetchGroupPasswds(groupId)
                    }
                }

                is UiAction.ShowPasswd -> {
                    val passwd = getGroupPasswd(passwdId)
                    updatePasswdUiState {
                        copy(selectPasswd = passwd)
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
                    val selectGroupId = groupUiState.value.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        deleteGroup(selectGroupId)
                    }
                }

                is UiAction.UpdateGroup -> {
                    val selectGroupId = groupUiState.value.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        updateGroup(
                            groupId = selectGroupId,
                            groupName = groupName,
                            groupComment = groupComment
                        )
                    }
                }

                is UiAction.ClearEffect -> {
                    updateDialogUiState {
                        copy(effect = null)
                    }
                }

                is UiAction.NewPasswd -> {
                    launch(Dispatchers.IO) {
                        newPasswd(
                            groupId = groupId,
                            title = title,
                            usernameString = usernameString,
                            passwordString = passwordString,
                            link = link,
                            comment = comment,
                        )
                    }
                }

                is UiAction.UpdatePasswd -> {
                    launch(Dispatchers.IO) {
                        updatePasswd(passwd)
                    }
                }

                is UiAction.DeletePasswd -> {
                    val selectGroupId = passwdUiState.value.selectPasswd?.id ?: return
                    launch(Dispatchers.IO) {
                        deletePasswd(selectGroupId)
                    }
                }

                is UiAction.MenuOpenOrClose -> {
                    updateWindowUiState {
                        copy(menuOpen = open)
                    }
                }

                else -> {}
            }
        }
    }

    private fun getGroupPasswd(passwdId: Int): Passwd? =
        passwdUiState.value.groupPasswds.find { passwd: Passwd -> passwd.id == passwdId }

    private fun getGroup(groupId: Int): Group? =
        groupUiState.value.groups.find { group: Group -> group.id == groupId }


    /**
     * 更新页面状态
     * 调用时在函数块中用 data class 的 copy函数就行
     */
    private fun updateWindowUiState(update: WindowUiState.() -> WindowUiState) {
        _windowUiState.update { update(it) }
    }

    private fun updateGroupUiState(update: GroupUiState.() -> GroupUiState) {
        _groupUiState.update { update(it) }
    }

    private fun updatePasswdUiState(update: PasswdUiState.() -> PasswdUiState) {
        _passwdUiState.update { update(it) }
    }

    private fun updateDialogUiState(update: DialogUiState.() -> DialogUiState) {
        _dialogUiState.update { update(it) }
    }

    init {
        launch {
            shouldBeLandscape.stateIn(this, SharingStarted.WhileSubscribed(), WindowUiState.Default.isLandscape)
                .collectLatest {
                    updateWindowUiState { copy(isLandscape = it) }
                }
        }
    }

}