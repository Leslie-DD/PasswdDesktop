import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.Res
import model.UiAction
import model.viewmodel.ConfigViewModel
import model.viewmodel.PasswdsViewModel
import ui.App

fun main() = application {
    val state = rememberWindowState(width = 1200.dp, height = 800.dp)

    val passwdsViewModel = remember { PasswdsViewModel() }
    val configViewModel = remember { ConfigViewModel() }

    Tray(
        icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
        onAction = { passwdsViewModel.onAction(UiAction.WindowVisible(true)) },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = { passwdsViewModel.onAction(UiAction.WindowVisible(true)) })
        Separator()
        Item("Exit App", onClick = ::exitApplication)
    }

    Window(
        onCloseRequest = { passwdsViewModel.onAction(UiAction.WindowVisible(false)) },
        visible = passwdsViewModel.windowUiState.collectAsState().value.windowVisible,
        title = "Passwd",
        state = state,
    ) {

        window.rootPane.apply {
            rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }

        val theme by configViewModel.theme.collectAsState()
        MaterialTheme(colorScheme = theme.materialColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    App(
                        passwdsViewModel = passwdsViewModel,
                        configViewModel = configViewModel
                    )
                }
            }
        }
    }
}
