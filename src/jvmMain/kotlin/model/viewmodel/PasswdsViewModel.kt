package model.viewmodel

import com.google.gson.Gson
import database.DataBase
import database.entity.HistoryData
import database.entity.HistoryData.Companion.defaultHistoryData
import entity.Group
import entity.IDragAndDrop
import entity.LoginResult
import entity.Passwd
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import model.UiAction
import model.UiScreen
import model.UiScreens
import model.uieffect.DialogUiEffect
import model.uistate.DialogUiState
import model.uistate.LoginUiState
import model.uistate.PasswdUiState
import model.uistate.WindowUiState
import network.HttpClientObj
import network.WebSocketSyncUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.PasswdRepository
import utils.FileUtils

@OptIn(FlowPreview::class)
class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val repository: PasswdRepository = PasswdRepository()

    private val _windowUiState = MutableStateFlow(WindowUiState.Default)
    val windowUiState: StateFlow<WindowUiState> = _windowUiState

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
        MutableStateFlow(LoginUiState(defaultHistoryData(), dataBase.getSavedHistories()))
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

    private val dataBase = DataBase.instance

    init {
        launch {
            silentlyLogin()
        }

        launch {
            repository.groupsFlow.collect {
                logger.debug("collect groups changed, size: ${it.size}")
                updateGroupUiState {
                    copy(
                        groups = it,
                        selectGroup = if (it.isEmpty()) null else it.first()
                    )
                }
            }
        }

        launch {
            repository.groupPasswdsFlow.collect {
                logger.debug("collect groupPasswds changed, size: ${it.size}")
                updateGroupUiState {
                    copy(
                        groupPasswds = it,
                        selectPasswd = if (it.isEmpty()) null else it.first()
                    )
                }
            }
        }

        launch {
            searchFlow.debounce(300).collectLatest {
                repository.searchLikePasswdsAndUpdate(it)
                updateGroupUiState {
                    copy(selectGroup = null)
                }
            }
        }

        launch {
            val all = dataBase.getAll()
            logger.info("database.all: $all")
        }
    }

    private suspend fun silentlyLogin() {
        val savedHistoryData = dataBase.latestSavedLoginHistoryData()
        if (savedHistoryData == null) {
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Login,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            return
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
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Login,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
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
        if (username.isBlank() || password.isBlank() || secretKey.isBlank()) {
            logger.warn("(loginByPassword) warn, $username, $password, $secretKey")
            updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure("username, password and secret key can not be null")) }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Login,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            return
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            logger.warn("(loginByPassword) warn, ${e.message}")
            updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure(e.message)) }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Login,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            return
        }

        repository.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess {
            logger.info("(loginByPassword) success")
            updateDB(username, password, secretKey, host, port, it.token, saved, silentlyLogin)
            onLoginSuccess(host, port, secretKey, it)
        }.onFailure {
            logger.error("(loginByPassword) error: ${it.message}")
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndSignupFailure(it.message))
            }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Login,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            it.printStackTrace()
        }
    }

    private suspend fun onLoginSuccess(host: String, port: Int, secretKey: String, loginResult: LoginResult) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                dataBase.globalSecretKey.emit(secretKey)
                dataBase.globalUserId.emit(loginResult.userId)
                dataBase.globalUsername.emit(loginResult.username)
                dataBase.globalAccessToken.emit(loginResult.token)
            }
            repository.fetchGroups()
            updateDialogUiState {
                copy(effect = null)
            }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Passwds,
                    uiScreens = UiScreen.LoggedInScreen
                )
            }
        }

        WebSocketSyncUtil.startWebSocketListener(host, port, loginResult.userId)
    }

    private suspend fun signup(
        username: String,
        password: String,
        host: String,
        port: Int,
    ) {
        if (username.isBlank() || password.isBlank()) {
            logger.warn("sign up username and password can not be empty")
            updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure("sign up username and password can not be empty")) }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Signup,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            return
        }

        try {
            HttpClientObj.forceUpdateHttpClient(host, port)
        } catch (e: Throwable) {
            logger.warn("(loginByPassword) warn, ${e.message}")
            updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure(e.message)) }
            updateWindowUiState {
                copy(
                    uiScreen = UiScreen.Signup,
                    uiScreens = UiScreen.LoginAndSignup
                )
            }
            return
        }

        repository.signup(
            username = username,
            password = password,
        ).onSuccess {
            if (it == null) {
                return@onSuccess
            }
            updateDialogUiState {
                copy(effect = DialogUiEffect.SignupResult(it.secretKey))
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
            updateDialogUiState {
                copy(effect = DialogUiEffect.LoginAndSignupFailure(it.message))
            }
            it.printStackTrace()
        }
    }

    private suspend fun fetchGroupPasswds(groupId: Int) {
        repository.updateGroupPasswds(groupId)
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
                updateGroupUiState {
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
            updateGroupUiState {
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
            updateGroupUiState {
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
                updateGroupUiState {
                    copy(selectPasswd = null)
                }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateDialogUiState {
                    copy(effect = DialogUiEffect.DeletePasswdResult(null))
                }
            }
    }

    private fun updateDB(
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
            defaultHistoryData()
        } else {
            insertHistoryData
        }
        updateLoginUiState {
            copy(
                historyData = historyData,
                historyDataList = dataBase.getSavedHistories()
            )
        }
        logger.info("(saveLoginInfo) insertResultId: $insertResultId, ${loginUiState.value}")
    }

    fun onAction(action: UiAction) {
        logger.debug("onAction: {}", action)
        with(action) {
            when (this) {
                is UiAction.GoScreen -> {
                    val uiScreens: UiScreens? = when (screen) {
                        in UiScreen.LoginAndSignup -> UiScreen.LoginAndSignup
                        in UiScreen.LoggedInScreen -> UiScreen.LoggedInScreen
                        in UiScreen.Loadings -> UiScreen.Loadings
                        else -> null
                    }
                    if (uiScreens == null) {
                        updateWindowUiState {
                            copy(uiScreen = screen)
                        }
                    } else {
                        updateWindowUiState {
                            copy(
                                uiScreen = screen,
                                uiScreens = uiScreens
                            )
                        }
                    }
                }

                is UiAction.WindowVisible -> {
                    updateWindowUiState {
                        copy(windowVisible = visible)
                    }
                }

                is UiAction.ShowGroupPasswds -> {
                    updateGroupUiState {
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
                    updateGroupUiState {
                        copy(selectPasswd = passwd)
                    }
                }

                is UiAction.Login -> {
                    launch(Dispatchers.IO) {
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
                }

                is UiAction.Signup -> {
                    launch(Dispatchers.IO) {
                        signup(
                            username = username,
                            password = password,
                            host = host,
                            port = port
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
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        deleteGroup(selectGroupId)
                    }
                }

                is UiAction.UpdateGroup -> {
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
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

                is UiAction.ReorderGroupDragEnd -> {
                    /**
                     * TODO: 服务端实现。
                     * 暂时服务端未实现，所以直接更新GroupUiState，
                     * 但是repository.groupsFlow未更新，违反了单一数据源的原则，
                     * 所以目前会有重新排序后，会有添加删除 Group，UI数据都不会更新的问题
                     */
                    updateGroupUiState {
                        copy(groups = reorderedGroupList)
                    }
                }

                is UiAction.ExportPasswdsToFile -> {
                    launch {
                        val json = Gson().toJson(repository.getAllGroupsWithPasswds())
                        FileUtils.exportDataToFile(filePath, json)
                    }
                }

                is UiAction.InitHost -> {
                    // TODO: host valid check
                }

            }
        }
    }

    fun onPasswdListItemDragEnter(item: Passwd, dynamicItem: IDragAndDrop) {
        if (dynamicItem is Passwd) {
            val passwdList = passwdUiState.value.groupPasswds.toMutableList().apply {
                val index = indexOf(item)
                if (index == -1) {
                    return
                }
                remove(dynamicItem)
                add(index, dynamicItem)
            }
            updateGroupUiState {
                copy(groupPasswds = passwdList)
            }
        }
    }

    fun onGroupListDragEnter(dynamicItem: IDragAndDrop) {
        if (dynamicItem is Passwd) {
            val passwdList = passwdUiState.value.groupPasswds.toMutableList().apply {
                if (!remove(dynamicItem)) {
                    return
                }
            }
            updateGroupUiState {
                copy(groupPasswds = passwdList)
            }
        }

        // TODO: 往Group中添加Passwd
    }

    fun onGroupListItemDragEnter(item: Group, dynamicItem: IDragAndDrop) {
        if (dynamicItem is Passwd) {
            val passwdList = passwdUiState.value.groupPasswds.toMutableList().apply {
                remove(dynamicItem)
            }
            updateGroupUiState {
                copy(groupPasswds = passwdList)
            }

            logger.info("will put the passwd into the groups[${passwdUiState.value.groups.indexOf(item)}]")
            // TODO: 往Group中添加Passwd
        } else if (dynamicItem is Group) {
            val groupList = passwdUiState.value.groups.toMutableList().apply {
                val index = indexOf(item)
                if (index == -1) {
                    return
                }
                remove(dynamicItem)
                add(index, dynamicItem)
            }
            updateGroupUiState {
                copy(groups = groupList)
            }
        }
    }

    private fun getGroupPasswd(passwdId: Int): Passwd? =
        passwdUiState.value.groupPasswds.find { passwd: Passwd -> passwd.id == passwdId }

    private fun getGroup(groupId: Int): Group? =
        passwdUiState.value.groups.find { group: Group -> group.id == groupId }

    /**
     * 更新页面状态
     * 调用时在函数块中用 data class 的 copy函数就行
     */
    private fun updateWindowUiState(update: WindowUiState.() -> WindowUiState) {
        _windowUiState.update { update(it) }
    }

    private fun updateGroupUiState(update: PasswdUiState.() -> PasswdUiState) {
        _passwdUiState.update {
            update(it)
        }
    }

    private fun updateDialogUiState(update: DialogUiState.() -> DialogUiState) {
        _dialogUiState.update { update(it) }
    }

    private fun updateLoginUiState(update: LoginUiState.() -> LoginUiState) {
        _loginUiState.update { update(it) }
    }

}