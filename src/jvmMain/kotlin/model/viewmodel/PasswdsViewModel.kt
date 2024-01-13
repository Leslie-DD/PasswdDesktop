package model.viewmodel

import com.google.gson.Gson
import entity.Group
import entity.IDragAndDrop
import entity.Passwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import model.UiScreen
import model.UiScreens
import model.action.PasswdAction
import model.uieffect.DialogUiEffect
import model.uistate.DialogUiState
import model.uistate.PasswdUiState
import model.uistate.WindowUiState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.PasswdRepository
import repository.UserRepository
import utils.FileUtils

@OptIn(FlowPreview::class)
class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val passwdRepository: PasswdRepository = PasswdRepository
    private val userRepository: UserRepository = UserRepository

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

    private val _searchFlow: MutableStateFlow<String> by lazy {
        MutableStateFlow("")
    }
    private val searchFlow: StateFlow<String> by lazy {
        _searchFlow
    }

    init {
        launch {
            passwdRepository.groupsFlow.collect {
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
            passwdRepository.groupPasswdsFlow.collect {
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
                passwdRepository.searchLikePasswdsAndUpdate(it)
                updateGroupUiState {
                    copy(selectGroup = null)
                }
            }
        }

        collectLoginResult()
        collectSignupResult()
    }

    private fun collectLoginResult() = launch {
        userRepository.loginResultStateFlow.filterNotNull().collectLatest {
            it.onSuccess {
                updateDialogUiState {
                    copy(effect = null)
                }
                updateWindowUiState {
                    copy(
                        uiScreen = UiScreen.Passwds,
                        uiScreens = UiScreen.LoggedInScreen
                    )
                }
            }.onFailure {
                logger.error("(loginByPassword) error", it)
                updateDialogUiState {
                    copy(effect = if (it.message.isNullOrBlank()) null else DialogUiEffect.LoginAndSignupFailure(it.message))
                }
                updateWindowUiState {
                    copy(
                        uiScreen = UiScreen.Login,
                        uiScreens = UiScreen.LoginAndSignup
                    )
                }
            }
        }
    }

    private fun collectSignupResult() {
        launch {
            userRepository.signResultStateFlow.filterNotNull().collectLatest {
                it.onSuccess { signupResult ->
                    if (signupResult == null) {
                        return@onSuccess
                    }
                    updateDialogUiState {
                        copy(effect = DialogUiEffect.SignupResult(signupResult.secretKey))
                    }
                }.onFailure {
                    updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure(it.message)) }
                    updateWindowUiState {
                        copy(
                            uiScreen = UiScreen.Signup,
                            uiScreens = UiScreen.LoginAndSignup
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshGroupPasswds(groupId: Int) {
        passwdRepository.refreshGroupPasswds(groupId)
    }

    private suspend fun newGroup(
        groupName: String,
        groupComment: String
    ) {
        if (groupName.isBlank()) {
            // TODO: tips 提示 groupName 不能为空
            return
        }
        passwdRepository.newGroup(groupName, groupComment)
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
        passwdRepository.deleteGroup(groupId)
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
        passwdRepository.updateGroup(
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
        passwdRepository.newPasswd(
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
        passwdRepository.updatePasswd(
            id = updatePasswd.id,
            title = updatePasswd.title,
            usernameString = updatePasswd.usernameString,
            passwordString = updatePasswd.passwordString,
            link = updatePasswd.link,
            comment = updatePasswd.comment
        ).onSuccess { passwd ->
            logger.info("updatePasswd onSuccess $passwd")
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
        passwdRepository.deletePasswd(passwdId)
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

    fun onAction(action: PasswdAction) {
        logger.info("onAction: {}", action)
        with(action) {
            when (this) {
                is PasswdAction.GoScreen -> {
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

                is PasswdAction.ShowGroupPasswds -> {
                    updateGroupUiState {
                        copy(
                            selectGroup = getGroup(groupId),
                            selectPasswd = null
                        )
                    }
                    launch(Dispatchers.IO) {
                        refreshGroupPasswds(groupId)
                    }
                }

                is PasswdAction.ShowPasswd -> {
                    val passwd = getGroupPasswd(passwdId)
                    updateGroupUiState {
                        copy(selectPasswd = passwd)
                    }
                }

                is PasswdAction.NewGroup -> {
                    launch(Dispatchers.IO) {
                        newGroup(
                            groupName = groupName,
                            groupComment = groupComment
                        )
                    }
                }

                is PasswdAction.DeleteGroup -> {
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        deleteGroup(selectGroupId)
                    }
                }

                is PasswdAction.UpdateGroup -> {
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
                    launch(Dispatchers.IO) {
                        updateGroup(
                            groupId = selectGroupId,
                            groupName = groupName,
                            groupComment = groupComment
                        )
                    }
                }

                is PasswdAction.ClearEffect -> {
                    updateDialogUiState {
                        copy(effect = null)
                    }
                }

                is PasswdAction.NewPasswd -> {
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

                is PasswdAction.UpdatePasswd -> {
                    launch(Dispatchers.IO) {
                        updatePasswd(passwd)
                    }
                }

                is PasswdAction.DeletePasswd -> {
                    val selectGroupId = passwdUiState.value.selectPasswd?.id ?: return
                    launch(Dispatchers.IO) {
                        deletePasswd(selectGroupId)
                    }
                }

                is PasswdAction.MenuOpenOrClose -> {
                    updateWindowUiState {
                        copy(menuOpen = open)
                    }
                }

                is PasswdAction.SearchPasswds -> {
                    _searchFlow.tryEmit(content)
                }

                is PasswdAction.ReorderGroupDragEnd -> {
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

                is PasswdAction.ExportPasswdsToFile -> {
                    launch {
                        val json = Gson().toJson(passwdRepository.getAllGroupsWithPasswds())
                        FileUtils.exportDataToFile(filePath, json)
                    }
                }

                is PasswdAction.InitHost -> {
                    // TODO: host valid check
                }

                is PasswdAction.UpdateEditEnabled -> {
                    _passwdUiState.update {
                        it.copy(editEnabled = editEnabled)
                    }
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
        _passwdUiState.update {
            it.copy(editEnabled = false)
        }
    }

    private fun updateDialogUiState(update: DialogUiState.() -> DialogUiState) {
        _dialogUiState.update { update(it) }
    }

}