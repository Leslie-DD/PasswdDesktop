package passwds.model

import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.KtorRequest
import org.jetbrains.skia.impl.Log
import passwds.entity.Passwd
import platform.desktop.Platform
import platform.desktop.currentPlatform

class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val _passwds = MutableStateFlow<List<Passwd>>(emptyList())
    val passwds: StateFlow<List<Passwd>> get() = _passwds

    val scaffoldState = ScaffoldState(DrawerState(DrawerValue.Closed), SnackbarHostState())

    val shouldBeLandscape = MutableStateFlow(currentPlatform == Platform.Desktop)

    private val _uiState = MutableStateFlow(TranslateUiState.Default)
    private val translateUiState
        get() = _uiState.value
    val uiState: TranslateUiState
        @Composable get() = _uiState.collectAsState().value


    val exitApp = MutableStateFlow(false)

    suspend fun fetchPasswds() = withContext(Dispatchers.IO) {
        Log.debug("PasswdsViewModel().fetchPasswds start")
        KtorRequest.postPasswds()
            .onSuccess {
                Log.info("PasswdsViewModel().fetchPasswds success, size:${it.size}")
                _passwds.value = it
            }.onFailure {
                Log.error("PasswdsViewModel().fetchPasswds error:${it.message}")
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

                else -> {}
            }
        }
    }

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