package passwds.model

import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import config.LocalPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Setting
import org.jetbrains.skia.impl.Log
import passwds.entity.Group
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
            fetchPasswds()
        }
        launch(Dispatchers.IO) {
            fetchGroups()
        }
    }

    private suspend fun fetchPasswds() {
        Log.debug("PasswdsViewModel().fetchPasswds start")
        repository.fetchPasswds()
            .onSuccess {
                Log.info("PasswdsViewModel().fetchPasswds success, size:${it.size}")
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
        Log.debug("PasswdsViewModel().fetchGroups start")
        repository.fetchGroups()
            .onSuccess {
                Log.info("PasswdsViewModel().fetchGroups success, size:${it.size}")
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
        Log.debug("PasswdsViewModel().fetchGroupPasswds start")
        repository.fetchGroupPasswds(groupId)
            .onSuccess {
                Log.info("PasswdsViewModel().fetchGroupPasswds success, size:${it.size}")
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

    fun onAction(action: TranslateScreenUiAction) {
        with(action) {
            when (this) {
                is TranslateScreenUiAction.GoScreen -> {
                    updateUiState { copy(uiScreen = screen) }
                }

                is TranslateScreenUiAction.ExitApp -> {
                    exitApp.tryEmit(true)
                }

                is TranslateScreenUiAction.WindowVisible -> {
                    updateUiState {
                        copy(windowVisible = visible)
                    }
                }

                is TranslateScreenUiAction.ShowGroupPasswds -> {
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

                is TranslateScreenUiAction.ShowPasswd -> {
                    updateUiState {
                        copy(
                            selectPasswd = getPasswd(passwdId)
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