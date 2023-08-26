import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.Res
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.ui.App
import theme.LocalSpacing
import theme.Spacing

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
        onAction = { viewModel.onAction(UiAction.WindowVisible(true)) },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = { viewModel.onAction(UiAction.WindowVisible(true)) })
        Separator()
        Item("Exit App", onClick = ::exitApplication)
    }
    val windowUiState = viewModel.windowUiState.collectAsState().value
    Window(
        onCloseRequest = { viewModel.onAction(UiAction.WindowVisible(false)) },
        visible = windowUiState.windowVisible,
        title = "Passwd",
        state = state,
    ) {

        val theme by viewModel.theme.collectAsState()
        CompositionLocalProvider(LocalSpacing provides Spacing()) {
            MaterialTheme(colorScheme = theme.materialColorScheme) {
                Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
                    App(viewModel = viewModel)
                }
            }
        }

//        MenuBar {
//            Menu("File") {
//                Item("New window", onClick = { })
//                Menu("File") {
//                    Item("New window", onClick = { })
//                    Item("Exit", onClick = { })
//                }
//                Item("设置", onClick = {
//                    viewModel.onAction(UiAction.GoScreen(UiScreen.Settings))
//                }, icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER))
//            }
//        }
    }
}

/**
 * 认为 4:3 是区分横竖屏的分界点
 */
private val DpSize.isLandscape: Boolean
    get() = (height / width) > (4f / 3f)
