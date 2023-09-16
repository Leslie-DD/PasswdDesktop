import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
    val state = rememberWindowState(width = 1200.dp, height = 800.dp)

    val viewModel = remember { PasswdsViewModel() }
    viewModel.shouldBeLandscape.tryEmit(!state.size.isLandscape)

    Tray(
        icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
        onAction = { viewModel.onAction(UiAction.WindowVisible(true)) },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = { viewModel.onAction(UiAction.WindowVisible(true)) })
        Separator()
        Item("Exit App", onClick = ::exitApplication)
    }

    Window(
        onCloseRequest = { viewModel.onAction(UiAction.WindowVisible(false)) },
        visible = viewModel.windowUiState.collectAsState().value.windowVisible,
        title = "Passwd",
        state = state,
    ) {

        window.rootPane.apply {
            rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }

        CompositionLocalProvider(LocalSpacing provides Spacing()) {
            val theme by viewModel.theme.collectAsState()
            MaterialTheme(colorScheme = theme.materialColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        App(viewModel = viewModel)
                    }
                }
            }
        }

    }
}

/**
 * 认为 4:3 是区分横竖屏的分界点
 */
private val DpSize.isLandscape: Boolean
    get() = (height / width) > (4f / 3f)
