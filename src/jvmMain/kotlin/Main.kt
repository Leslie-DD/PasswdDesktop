import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import datasource.sys.OS
import datasource.sys.getOperatingSystem
import model.Res
import model.UiScreen
import model.action.UiAction
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UiConfigViewModel
import model.viewmodel.UserViewModel
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
    val state = rememberWindowState(width = 1100.dp, height = 800.dp)

    val userViewModel = remember { UserViewModel() }
    val passwdsViewModel = remember { PasswdsViewModel() }
    val uiConfigViewModel = remember { UiConfigViewModel() }

    val theme by uiConfigViewModel.theme.collectAsState()
    val windowUiState by passwdsViewModel.windowUiState.collectAsState()
    val windowVisible by uiConfigViewModel.windowVisible.collectAsState()

    Tray(
        icon = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
        onAction = { uiConfigViewModel.onAction(UiAction.WindowVisible(true)) },
        tooltip = if (getOperatingSystem() == OS.MAC_OS) "点击显示更多" else "双击打开应用",
    ) {
        Item(
            text = if (windowVisible) "Hide window" else "Show window",
            onClick = { uiConfigViewModel.onAction(UiAction.WindowVisible(!windowVisible)) }
        )
        Separator()
        Item("Exit", onClick = ::exitApplication)
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
            onCloseRequest = { uiConfigViewModel.onAction(UiAction.WindowVisible(false)) },
            visible = windowVisible,
            title = "Passwd",
            state = state,
            onKeyEvent = {
                when {
                    (it.isCtrlPressed && it.key == Key.F && it.type == KeyEventType.KeyDown) -> {
                        if (getOperatingSystem() != OS.MAC_OS) {
                            uiConfigViewModel.onAction(UiAction.FocusOnSearch(!uiConfigViewModel.searchFocus.value))
                            true
                        }
                        false
                    }

                    (it.isMetaPressed && it.key == Key.F && it.type == KeyEventType.KeyDown) -> {
                        if (getOperatingSystem() == OS.MAC_OS) {
                            uiConfigViewModel.onAction(UiAction.FocusOnSearch(!uiConfigViewModel.searchFocus.value))
                            true
                        }
                        false
                    }

                    (it.isMetaPressed && it.key == Key.W && it.type == KeyEventType.KeyDown) -> {
                        if (getOperatingSystem() == OS.MAC_OS) {
                            uiConfigViewModel.onAction(UiAction.WindowVisible(false))
                            true
                        }
                        false
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
                    TitleBarView(uiConfigViewModel, passwdsViewModel)
                }

                Surface(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        App(
                            userViewModel = userViewModel,
                            passwdsViewModel = passwdsViewModel,
                            uiConfigViewModel = uiConfigViewModel
                        )
                    }
                }
            }
        }
    }
}
