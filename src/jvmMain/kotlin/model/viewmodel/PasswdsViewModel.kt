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

    private val _passwdUiState: MutableStateFlow<PasswdUiState> by lazy { MutableStateFlow(PasswdUiState.defaultPasswdUiState()) }
    val passwdUiState: StateFlow<PasswdUiState> by lazy { _passwdUiState }

    private val _dialogUiState: MutableStateFlow<DialogUiState> by lazy { MutableStateFlow(DialogUiState.defaultDialogUiState()) }
    val dialogUiState: StateFlow<DialogUiState> by lazy { _dialogUiState }

    private val _searchFlow: MutableStateFlow<String> by lazy { MutableStateFlow("") }
    private val searchFlow: StateFlow<String> by lazy { _searchFlow }

    init {
        collectGroups()
        collectGroupPasswds()

        collectLoginResult()
        collectSignupResult()

        collectSearch()
    }

    private fun collectGroups() = launch {
        passwdRepository.groupsFlow.collect { groups ->
            logger.debug("collect groups changed, size: ${groups.size}")
            val selectGroup = if (groups.isEmpty()) null else groups.first()
            updateGroupUiState { copy(groups = groups, selectGroup = selectGroup) }
        }
    }

    private fun collectGroupPasswds() = launch {
        passwdRepository.groupPasswdsFlow.collect { groupPasswds ->
            logger.debug("collect groupPasswds changed, size: ${groupPasswds.size}")
            val selectPasswd = if (groupPasswds.isEmpty()) null else groupPasswds.first()
            updateGroupUiState { copy(groupPasswds = groupPasswds, selectPasswd = selectPasswd) }
        }
    }

    private fun collectLoginResult() = launch {
        userRepository.loginResultStateFlow.filterNotNull().collectLatest {
            var effect: DialogUiEffect? = null
            var uiScreen: UiScreen = UiScreen.Passwds
            var uiScreens: UiScreens = UiScreen.LoggedInScreen

            it.onSuccess {
                effect = null
                uiScreen = UiScreen.Passwds
                uiScreens = UiScreen.LoggedInScreen
                passwdRepository.fetchGroups()
            }.onFailure { e ->
                logger.error("(loginByPassword) error", e)
                effect = if (e.message.isNullOrBlank()) null else DialogUiEffect.LoginAndSignupFailure(e.message)
                uiScreen = UiScreen.Login
                uiScreens = UiScreen.LoginAndSignup
            }

            updateDialogUiState { copy(effect = effect) }
            updateWindowUiState { copy(uiScreen = uiScreen, uiScreens = uiScreens) }
        }
    }

    private fun collectSignupResult() {
        launch {
            userRepository.signResultStateFlow.filterNotNull().collectLatest {
                it.onSuccess { signupResult ->
                    if (signupResult == null) {
                        return@onSuccess
                    }
                    updateDialogUiState { copy(effect = DialogUiEffect.SignupResult(signupResult.secretKey)) }
                }.onFailure {
                    updateDialogUiState { copy(effect = DialogUiEffect.LoginAndSignupFailure(it.message)) }
                    updateWindowUiState { copy(uiScreen = UiScreen.Signup, uiScreens = UiScreen.LoginAndSignup) }
                }
            }
        }
    }

    private fun collectSearch() = launch {
        searchFlow.debounce(300).collectLatest {
            passwdRepository.searchLikePasswdsAndUpdate(it)
            updateGroupUiState { copy(selectGroup = null) }
        }
    }

    private fun newGroup(
        groupName: String,
        groupComment: String
    ) = launch {
        if (groupName.isBlank()) {
            // TODO: tips 提示 groupName 不能为空
            return@launch
        }
        passwdRepository.newGroup(groupName, groupComment)
            .onSuccess {
                updateDialogUiState { copy(effect = DialogUiEffect.NewGroupResult(it)) }
                updateGroupUiState { copy(selectGroup = passwdUiState.value.groups.last(), selectPasswd = null) }
            }.onFailure {
                // TODO: 新增失败的情况 tips 提示
                updateDialogUiState { copy(effect = DialogUiEffect.NewGroupResult(null)) }
            }
    }

    private fun deleteGroup(groupId: Int) = launch {
        passwdRepository.deleteGroup(groupId)
            .onSuccess {
                updateGroupUiState { copy(selectGroup = null) }
                updateDialogUiState { copy(effect = DialogUiEffect.DeleteGroupResult(it)) }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateDialogUiState { copy(effect = DialogUiEffect.DeleteGroupResult(null)) }
            }
    }

    private fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ) = launch {
        passwdRepository.updateGroup(groupId, groupName, groupComment)
            .onSuccess {
                updateDialogUiState { copy(effect = DialogUiEffect.UpdateGroupResult(it)) }
            }.onFailure {
                // TODO: 更新失败的情况 tips 提示
                updateDialogUiState { copy(effect = DialogUiEffect.UpdateGroupResult(null)) }
            }
    }

    private fun newPasswd(
        groupId: Int,
        title: String,
        usernameString: String,
        passwordString: String,
        link: String,
        comment: String,
    ) = launch {
        passwdRepository.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = usernameString,
            passwordString = passwordString,
            link = link,
            comment = comment,
        ).onSuccess {
            updateDialogUiState { copy(effect = DialogUiEffect.NewPasswdResult(it)) }
            updateGroupUiState { copy(selectPasswd = it) }
        }.onFailure {
            it.printStackTrace()
            updateDialogUiState { copy(effect = DialogUiEffect.NewGroupResult(null)) }
        }
    }


    private fun updatePasswd(
        updatePasswd: Passwd
    ) = launch {
        passwdRepository.updatePasswd(
            id = updatePasswd.id,
            groupId = updatePasswd.groupId,
            title = updatePasswd.title,
            usernameString = updatePasswd.usernameString,
            passwordString = updatePasswd.passwordString,
            link = updatePasswd.link,
            comment = updatePasswd.comment
        ).onSuccess { passwd ->
            logger.info("updatePasswd onSuccess $passwd")
            updateDialogUiState { copy(effect = DialogUiEffect.UpdatePasswdResult(passwd)) }
            updateGroupUiState { copy(selectPasswd = passwd) }
        }.onFailure {
            it.printStackTrace()
            updateDialogUiState { copy(effect = DialogUiEffect.UpdatePasswdResult(null)) }
        }
    }

    private fun deletePasswd(passwdId: Int) = launch {
        passwdRepository.deletePasswd(passwdId)
            .onSuccess {
                updateDialogUiState { copy(effect = DialogUiEffect.DeletePasswdResult(it)) }
                updateGroupUiState { copy(selectPasswd = null) }
            }.onFailure {
                // TODO: 删除失败的情况 tips 提示
                updateDialogUiState { copy(effect = DialogUiEffect.DeletePasswdResult(null)) }
            }
    }

    fun onAction(action: PasswdAction) {
        logger.info("onAction: {}", action)
        with(action) {
            when (this) {
                is PasswdAction.GoScreen -> {
                    val uiScreens: UiScreens = when (screen) {
                        in UiScreen.LoginAndSignup -> UiScreen.LoginAndSignup
                        in UiScreen.LoggedInScreen -> UiScreen.LoggedInScreen
                        in UiScreen.Loadings -> UiScreen.Loadings
                        else -> windowUiState.value.uiScreens
                    }
                    updateWindowUiState { copy(uiScreen = screen, uiScreens = uiScreens) }
                }

                is PasswdAction.ShowGroupPasswds -> {
                    updateGroupUiState { copy(selectGroup = getGroup(groupId), selectPasswd = null) }
                    launch { passwdRepository.refreshGroupPasswds(groupId) }
                }

                is PasswdAction.ShowPasswd -> updateGroupUiState { copy(selectPasswd = getGroupPasswd(passwdId)) }

                is PasswdAction.NewGroup -> newGroup(groupName, groupComment)

                is PasswdAction.DeleteGroup -> {
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
                    deleteGroup(selectGroupId)
                }

                is PasswdAction.UpdateGroup -> {
                    val selectGroupId = passwdUiState.value.selectGroup?.id ?: return
                    updateGroup(selectGroupId, groupName, groupComment)
                }

                is PasswdAction.ClearEffect -> updateDialogUiState { copy(effect = null) }

                is PasswdAction.NewPasswd -> newPasswd(
                    groupId = groupId,
                    title = title,
                    usernameString = usernameString,
                    passwordString = passwordString,
                    link = link,
                    comment = comment,
                )

                is PasswdAction.UpdatePasswd -> updatePasswd(passwd)

                is PasswdAction.DeletePasswd -> {
                    val selectGroupId = passwdUiState.value.selectPasswd?.id ?: return
                    deletePasswd(selectGroupId)
                }

                is PasswdAction.MenuOpenOrClose -> updateWindowUiState { copy(menuOpen = open) }

                is PasswdAction.SearchPasswds -> _searchFlow.tryEmit(content)

                is PasswdAction.ExportPasswdsToFile -> launch {
                    val json = Gson().toJson(passwdRepository.getAllGroupsWithPasswds())
                    FileUtils.exportDataToFile(filePath, json)
                }

                is PasswdAction.UpdateEditEnabled -> _passwdUiState.update { it.copy(editEnabled = editEnabled) }
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
            updateGroupUiState { copy(groupPasswds = passwdList) }
        }
    }

    fun onGroupListDragEnter(dynamicItem: IDragAndDrop) {
        if (dynamicItem is Passwd) {
            val passwdList = passwdUiState.value.groupPasswds.toMutableList().apply {
                if (!remove(dynamicItem)) {
                    return
                }
            }
            updateGroupUiState { copy(groupPasswds = passwdList) }
        }

        // TODO: 往Group中添加Passwd
    }

    fun onGroupListItemDragEnter(item: Group, dynamicItem: IDragAndDrop) {
        if (dynamicItem is Passwd) {
            val passwdList = passwdUiState.value.groupPasswds.toMutableList().apply {
                remove(dynamicItem)
            }
            updateGroupUiState { copy(groupPasswds = passwdList) }

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
            updateGroupUiState { copy(groups = groupList) }
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
        _passwdUiState.update { update(it) }
        _passwdUiState.update { it.copy(editEnabled = false) }
    }

    private fun updateDialogUiState(update: DialogUiState.() -> DialogUiState) {
        _dialogUiState.update { update(it) }
    }

}