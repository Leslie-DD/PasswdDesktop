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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.repository.PasswdRepository
import platform.desktop.Platform
import platform.desktop.currentPlatform

class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

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

        launch {
            repository.passwds.collect {
                updateUiState {
                    copy(passwds = it)
                }
            }
        }

        launch {
            logger.info("PasswdsViewModel() collect groups start")
            repository.groups.collect {
                logger.info("PasswdsViewModel() collect groups update $it")
                updateUiState {
                    copy(groups = it)
                }
            }
        }

        launch {
            repository.groupPasswds.collect {
                updateUiState {
                    copy(groupPasswds = it)
                }
            }
        }
    }

    private fun clearData() {
        updateUiState {
            copy(
                selectGroup = null,
                selectPasswd = null
            )
        }
    }

    private suspend fun loginByToken() {
        val username = LocalPref.username
        logger.debug("PasswdsViewModel().loginByToken, username: $username")
        if (username.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val token = LocalPref.accessToken
        logger.debug("PasswdsViewModel().loginByToken, token: $token")
        if (token.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        val secretKey = LocalPref.secretKey
        logger.debug("PasswdsViewModel().loginByToken, secretKey: $secretKey")
        if (secretKey.isBlank()) {
            updateUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        repository.loginByToken(
            username = username,
            token = token,
            secretKey = secretKey
        ).onSuccess {
            logger.info("PasswdsViewModel().loginByToken success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            logger.error("PasswdsViewModel().loginByToken error:${it.message}")
            it.printStackTrace()
            updateUiState { copy(uiScreen = UiScreen.Login) }
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
        repository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess {
            logger.info("PasswdsViewModel().loginByPassword success, result: $it")
            onLoginSuccess(secretKey, it)
        }.onFailure {
            logger.error("PasswdsViewModel().loginByPassword error: ${it.message}")
            updateUiState {
                copy(effect = UiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
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
            updateUiState {
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
        repository.register(
            username = username,
            password = password,
        ).onSuccess {
            logger.info("PasswdsViewModel().register success, result: $it")
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
            logger.error("PasswdsViewModel().register error: ${it.message}")
            updateUiState {
                copy(effect = UiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun fetchPasswds() {
        logger.debug("PasswdsViewModel().fetchPasswds start")
        repository.fetchPasswds()
    }

    private suspend fun fetchGroups() {
        logger.debug("PasswdsViewModel().fetchGroups start")
        repository.fetchGroups()
    }

    private suspend fun fetchGroupPasswds(groupId: Int) = withContext(Dispatchers.IO) {
        logger.debug("PasswdsViewModel().fetchGroupPasswds start")
        repository.fetchGroupPasswds(groupId)
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
                        groupPasswds = arrayListOf()
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
                        groupPasswds = arrayListOf(),
                        selectGroup = null,
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

    private suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ) {
        repository.updateGroup(
            groupId = groupId,
            groupName = groupName,
            groupComment = groupComment
        )
            .onSuccess {
                val updateGroup = getGroup(groupId)
                updateGroup?.let {
                    it.groupName = groupName
                    it.groupComment = groupComment
                }
                updateUiState {
                    copy(
                        groups = groups,
                        effect = UiEffect.UpdateGroupResult(updateGroup)
                    )
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateUiState {
                    copy(
                        effect = UiEffect.UpdateGroupResult(null)
                    )
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
        repository.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = usernameString,
            passwordString = passwordString,
            link = link,
            comment = comment,
        )
            .onSuccess {
                logger.info("PasswdsViewModel().newPasswd onSuccess, id: $it")
                val newPasswd = Passwd(
                    id = it,
                    groupId = groupId,
                    title = title,
                    usernameString = usernameString,
                    passwordString = passwordString,
                    link = link,
                    comment = comment,
                    userId = settings.userId.value
                )
                updateUiState {
                    copy(
                        groupPasswds = groupPasswds.apply { add(newPasswd) },
                        effect = UiEffect.NewPasswdResult(newPasswd),
                        selectPasswd = newPasswd,
                    )
                }
            }.onFailure {
                logger.error("PasswdsViewModel().newPasswd onFailure")
                it.printStackTrace()
                updateUiState {
                    copy(
                        effect = UiEffect.NewGroupResult(null)
                    )
                }
            }
    }


    private suspend fun updatePasswd(
        updatePasswd: Passwd
    ) {
        logger.debug("PasswdsViewModel().updatePasswd start")
        delay(1000)
        repository.updatePasswd(
            id = updatePasswd.id,
            title = updatePasswd.title,
            usernameStr = updatePasswd.usernameString,
            passwordStr = updatePasswd.passwordString,
            link = updatePasswd.link,
            comment = updatePasswd.comment
        )
            .onSuccess { count ->
                logger.info("PasswdsViewModel().updatePasswd onSuccess, count: $count")
                updateUiState {
                    val originPasswd = getGroupPasswd(updatePasswd.id)
                    originPasswd?.let {
                        it.title = updatePasswd.title
                        it.usernameString = updatePasswd.usernameString
                        it.passwordString = updatePasswd.passwordString
                        it.link = updatePasswd.link
                        it.comment = updatePasswd.comment
                    }
                    copy(
                        groupPasswds = groupPasswds,
                        effect = UiEffect.UpdatePasswdResult(updatePasswd),
                        selectPasswd = updatePasswd,
                    )
                }
            }.onFailure {
                logger.error("PasswdsViewModel().updatePasswd onFailure")
                it.printStackTrace()
                updateUiState {
                    copy(
                        effect = UiEffect.UpdatePasswdResult(null)
                    )
                }
            }
    }

    private suspend fun deletePasswd(passwdId: Int) {
        repository.deletePasswd(passwdId)
            .onSuccess {
                val deletePasswd = getGroupPasswd(passwdId)
                updateUiState {
                    copy(
                        groupPasswds = groupPasswds.apply {
                            remove(deletePasswd)
                        },
                        selectPasswd = null,
                        effect = UiEffect.DeletePasswdResult(deletePasswd)
                    )
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateUiState {
                    copy(
                        effect = UiEffect.DeletePasswdResult(null)
                    )
                }
            }
    }

    fun onAction(action: UiAction) {
        logger.debug("onAction: $action")
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
                    val passwd = getGroupPasswd(passwdId)
                    updateUiState {
                        copy(
                            selectPasswd = passwd
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

                is UiAction.UpdateGroup -> {
                    val selectGroupId = translateUiState.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        updateGroup(
                            groupId = selectGroupId,
                            groupName = groupName,
                            groupComment = groupComment
                        )
                    }
                }

                is UiAction.ClearEffect -> {
                    updateUiState {
                        copy(
                            effect = null
                        )
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
                    val selectGroupId = translateUiState.selectPasswd?.id ?: return
                    launch(Dispatchers.IO) {
                        deletePasswd(selectGroupId)
                    }
                }

                else -> {}
            }
        }
    }

    private fun getGroupPasswd(passwdId: Int): Passwd? =
        translateUiState.groupPasswds.find { passwd: Passwd -> passwd.id == passwdId }

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