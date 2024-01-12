import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.Res
import model.UiAction
import model.UiScreen
import model.viewmodel.ConfigViewModel
import model.viewmodel.PasswdsViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.styling.TitleBarStyle
import ui.App
import ui.toolbar.TitleBarView

fun main() = application {
    val state = rememberWindowState(width = 1200.dp, height = 800.dp)

    val passwdsViewModel = remember { PasswdsViewModel() }
    val configViewModel = remember { ConfigViewModel() }

    val theme by configViewModel.theme.collectAsState()
    val windowUiState by passwdsViewModel.windowUiState.collectAsState()

    Tray(
        icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
        onAction = { passwdsViewModel.onAction(UiAction.WindowVisible(true)) },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = { passwdsViewModel.onAction(UiAction.WindowVisible(true)) })
        Separator()
        Item("Exit App", onClick = ::exitApplication)
    }

    IntUiTheme(
        theme = if (theme.isDark) {
            JewelTheme.darkThemeDefinition()
        } else {
            JewelTheme.lightThemeDefinition()
        },
        styling = ComponentStyling.decoratedWindow(
            titleBarStyle = if (theme.isDark) {
                TitleBarStyle.dark()
            } else {
                TitleBarStyle.light()
            }
        ),
        swingCompatMode = false
    ) {
        DecoratedWindow(
            onCloseRequest = { passwdsViewModel.onAction(UiAction.WindowVisible(false)) },
            visible = windowUiState.windowVisible,
            title = "Passwd",
            state = state,
            onKeyEvent = {
                when {
                    (it.isCtrlPressed && it.key == Key.F && it.type == KeyEventType.KeyDown) -> {
                        passwdsViewModel.onAction(UiAction.FocusOnSearch(!windowUiState.searchFocus))
                        true
                    }

                    else -> false
                }
            }
        ) {
            window.rootPane.apply {
                rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
            }

            MaterialTheme(colorScheme = theme.materialColorScheme) {
                var titleBarVisible by remember { mutableStateOf(false) }
                titleBarVisible = when (windowUiState.uiScreen) {
                    is UiScreen.Loading -> false
                    else -> true
                }
                if (titleBarVisible) {
                    TitleBarView(configViewModel, passwdsViewModel)
                }

                Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
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
}
