package passwds.model

import database.DataBase
import datamodel.HistoryData
import datamodel.HistoryData.Companion.defaultHistoryData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import model.Theme
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.model.uistate.*
import passwds.repository.PasswdRepository
import platform.desktop.Platform
import platform.desktop.currentPlatform

@OptIn(FlowPreview::class)
class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val repository: PasswdRepository = PasswdRepository()

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

    private val _loginUiState: MutableStateFlow<LoginUiState> by lazy {
        MutableStateFlow(LoginUiState(defaultHistoryData()))
    }
    val loginUiState: StateFlow<LoginUiState> by lazy {
        _loginUiState
    }

    private val _searchFlow: MutableStateFlow<String> by lazy {
        MutableStateFlow("")
    }
    private val searchFlow: StateFlow<String> by lazy {
        _searchFlow
    }

    val exitApp = MutableStateFlow(false)

    val theme = MutableStateFlow<Theme>(Theme.Light)

    private val dataBase = DataBase.instance

    init {
        launch(Dispatchers.IO) {
            silentlySignIn()
        }

        launch {
            repository.groups.collect {
                updateGroupUiState {
                    copy(groups = it)
                }
            }
        }

        launch {
            repository.groupPasswds.collect {
                updatePasswdUiState {
                    copy(groupPasswds = it)
                }
            }
        }

        launch {
            searchFlow.debounce(500).collectLatest {
                if (it.isNotBlank()) {
                    val passwds = repository.getAllPasswds(it)
                    updatePasswdUiState {
                        copy(
                            groupPasswds = passwds,
                            selectPasswd = null
                        )
                    }
                    updateGroupUiState {
                        copy(selectGroup = null)
                    }
                } else {
                    updatePasswdUiState {
                        copy(
                            groupPasswds = mutableListOf(),
                            selectPasswd = null
                        )
                    }
                    updateGroupUiState {
                        copy(selectGroup = null)
                    }
                }
            }
        }

        launch {
            val all = dataBase.getAll()
            logger.info("database.all: $all")
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

    private suspend fun silentlySignIn() {
        val savedHistoryData = dataBase.lastInsertHistory()
        if (savedHistoryData == null) {
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
            return
        }
        if (savedHistoryData.silentlySignIn) {
            loginByPassword(
                username = savedHistoryData.username,
                password = savedHistoryData.password,
                secretKey = savedHistoryData.secretKey,
                saved = savedHistoryData.saved,
                silentlySignIn = true,
                updateDb = false
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
            updateWindowUiState { copy(uiScreen = UiScreen.Login) }
        }
    }

    private suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String,
        saved: Boolean,
        silentlySignIn: Boolean,
        updateDb: Boolean = true
    ) {
        logger.debug("(loginByPassword) start")
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            logger.error("(loginByPassword) error, $username, $password, $secretKey")
            return
        }
        repository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess {
            logger.info("(loginByPassword) success, result: $it")
            if (updateDb) {
                saveLoginInfo(username, password, secretKey, it.token, saved, silentlySignIn)
            }
            onLoginSuccess(secretKey, it)
        }.onFailure {
            logger.error("(loginByPassword) error: ${it.message}")
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun onLoginSuccess(secretKey: String, loginResult: LoginResult) {
        logger.info("(onLoginSuccess)")
        updateWindowUiState { copy(uiScreen = UiScreen.Passwds) }
        coroutineScope {
            withContext(Dispatchers.IO) {
                dataBase.globalSecretKey.emit(secretKey)
                dataBase.globalUserId.emit(loginResult.userId)
                dataBase.globalUsername.emit(loginResult.username)
                dataBase.globalAccessToken.emit(loginResult.token)
                clearData()
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
        if (username.isBlank() || password.isBlank()) {
            // TODO: tips 提示
            logger.warn("register username and password can not be empty")
            return
        }
        repository.register(
            username = username,
            password = password,
        ).onSuccess {
            if (it == null) {
                return@onSuccess
            }
            updateDialogUiState {
                copy(effect = DialogUiEffect.RegisterResult(it.secretKey))
            }
            coroutineScope {
                withContext(Dispatchers.IO) {
                    dataBase.globalSecretKey.emit(it.secretKey)
                    dataBase.globalUserId.emit(it.userId)
                    dataBase.globalUsername.emit(username)
                    dataBase.globalAccessToken.emit(it.token)
                    clearData()
                }
            }
        }.onFailure {
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndRegisterFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun fetchGroups() {
        repository.fetchGroups()
    }

    private suspend fun fetchGroupPasswds(groupId: Int) {
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
                updateDialogUiState {
                    copy(effect = DialogUiEffect.NewGroupResult(it))
                }
                updateGroupUiState {
                    copy(selectGroup = it)
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
        repository.deleteGroup(groupId)
            .onSuccess {
                updateGroupUiState {
                    copy(selectGroup = null)
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
        repository.updateGroup(
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
        repository.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = usernameString,
            passwordString = passwordString,
            link = link,
            comment = comment,
        ).onSuccess {
            updateDialogUiState {
                copy(effect = DialogUiEffect.NewPasswdResult(it))
            }
            updatePasswdUiState {
                copy(selectPasswd = it)
            }
        }.onFailure {
            it.printStackTrace()
            updateDialogUiState {
                copy(effect = DialogUiEffect.NewGroupResult(null))
            }
        }
    }


    private suspend fun updatePasswd(
        updatePasswd: Passwd
    ) {
        repository.updatePasswd(
            id = updatePasswd.id,
            title = updatePasswd.title,
            usernameString = updatePasswd.usernameString,
            passwordString = updatePasswd.passwordString,
            link = updatePasswd.link,
            comment = updatePasswd.comment
        ).onSuccess { passwd ->
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdatePasswdResult(passwd))
            }
            updatePasswdUiState {
                copy(selectPasswd = passwd)
            }
        }.onFailure {
            it.printStackTrace()
            updateDialogUiState {
                copy(effect = DialogUiEffect.UpdatePasswdResult(null))
            }
        }
    }

    private suspend fun deletePasswd(passwdId: Int) {
        repository.deletePasswd(passwdId)
            .onSuccess {
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeletePasswdResult(it))
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

    private fun saveLoginInfo(
        username: String,
        password: String,
        secretKey: String,
        accessToken: String,
        saved: Boolean,
        silentlySignIn: Boolean
    ) {
        logger.info("(saveLoginInfo). username: $username, password: $password, secretKey: $secretKey, saved: $saved")
        var insertResultId = -1
        var insertHistoryData: HistoryData? = null
        dataBase.delete(null)
        if (saved) {
            insertHistoryData = HistoryData(
                username = username,
                password = password,
                secretKey = secretKey,
                accessToken = accessToken,
                saved = true,
                silentlySignIn = silentlySignIn
            )
            insertResultId = dataBase.insert(insertHistoryData)
        }
        val historyData = if (insertResultId == -1) {
            defaultHistoryData()
        } else {
            insertHistoryData!!
        }
        updateLoginUiState {
            copy(historyData = historyData)
        }
        logger.info("(saveLoginInfo) insertResultId: $insertResultId, ${loginUiState.value}")
    }

    fun onAction(action: UiAction) {
        logger.debug("onAction: {}", action)
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
                            secretKey = secretKey,
                            saved = saved,
                            silentlySignIn = silentlySignIn
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

                is UiAction.SearchPasswds -> {
                    _searchFlow.tryEmit(content)
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

    private fun updateLoginUiState(update: LoginUiState.() -> LoginUiState) {
        _loginUiState.update { update(it) }
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