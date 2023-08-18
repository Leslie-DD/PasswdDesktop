import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.Res
import passwds.model.PasswdsViewModel
import passwds.model.TranslateScreenUiAction
import passwds.ui.PasswdApp

fun main() = application {
    val state = rememberWindowState(width = 1000.dp)
    val viewModel = remember { PasswdsViewModel() }
    if (viewModel.exitApp.collectAsState().value) {
        exitApplication()
    }
    viewModel.shouldBeLandscape.tryEmit(!state.size.isLandscape)
    // 这是 菜单栏
    Tray(
        icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
        onAction = { viewModel.onAction(TranslateScreenUiAction.WindowVisible(true)) },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = { viewModel.onAction(TranslateScreenUiAction.WindowVisible(true)) })
        Separator()
        Item("Exit App", onClick = ::exitApplication)
    }
    Window(
        onCloseRequest = { viewModel.onAction(TranslateScreenUiAction.WindowVisible(false)) },
        visible = viewModel.uiState.windowVisible,
        title = "Passwd",
        state = state
    ) {
        PasswdApp(viewModel = viewModel)
    }
}

/**
 * 认为 4:3 是区分横竖屏的分界点
 */
private val DpSize.isLandscape: Boolean
    get() = (height / width) > (4f / 3f)
