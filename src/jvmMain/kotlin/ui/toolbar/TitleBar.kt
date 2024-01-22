package ui.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import model.UiScreen
import model.action.PasswdAction
import model.action.UiAction
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UiConfigViewModel
import org.jetbrains.jewel.intui.core.theme.IntUiDarkTheme
import org.jetbrains.jewel.intui.core.theme.IntUiLightTheme
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import java.net.URI

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DecoratedWindowScope.TitleBarView(
    uiConfigViewModel: UiConfigViewModel,
    passwdsViewModel: PasswdsViewModel
) {
    var pluginVisible by remember { mutableStateOf(false) }
    val windowUiState = passwdsViewModel.windowUiState.collectAsState().value
    pluginVisible = when (windowUiState.uiScreen) {
        is UiScreen.Passwds -> true
        else -> false
    }

    val theme = uiConfigViewModel.theme.collectAsState().value

    var fileChooserOpen by remember { mutableStateOf(false) }
    if (fileChooserOpen) {
        println("fileChooserOpen")
        SaveFileDialog(
            allowedExtensions = listOf(".json"),
            onCloseRequest = {
                fileChooserOpen = false
                println("Result $it")
                it?.let { passwdsViewModel.onAction(PasswdAction.ExportPasswdsToFile(it)) }
            }
        )
    }

    TitleBar(
        modifier = Modifier.newFullscreenControls(),
        gradientStartColor = if (theme.isDark) {
            Color(0xFF654B40)
        } else {
            Color(0xFFF5D4C1)
        },
        style = if (theme.isDark) {
            TitleBarStyle.dark(colors = TitleBarColors.Companion.dark())
        } else {
            TitleBarStyle.light(colors = TitleBarColors.Companion.customLight())
        }
    ) {
        Row(modifier = Modifier.align(Alignment.Start)) {
            Dropdown(
                enabled = pluginVisible,
                modifier = Modifier.height(30.dp),
                menuContent = {
                    selectableItem(
                        selected = false,
                        onClick = { fileChooserOpen = true },
                    ) {
                        ExportPasswdsFileIcon()
                    }
                }
            ) {
                Text("File")
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon("icons/jwd-icon-passWord.svg", "App Icon", StandaloneSampleIcons::class.java)
            Text(title)
        }

        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Tooltip({
                Text("Sync with Cloud")
            }) {
                val enable = false
                IconButton(
                    enabled = enable,
                    onClick = {

                    },
                    modifier = Modifier.size(40.dp).padding(5.dp)
                ) {
                    Icon(
                        resource = "icons/cloud-sync.svg",
                        contentDescription = "Sync",
                        iconClass = StandaloneSampleIcons::class.java,
                        tint = if (enable) Color(0xFF3A80FC) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Tooltip({
                Text("Search passwds (Ctrl + F)")
            }) {
                IconButton({
                    uiConfigViewModel.onAction(UiAction.FocusOnSearch(!uiConfigViewModel.searchFocus.value))
                }, Modifier.size(40.dp).padding(5.dp)) {
                    Icon("icons/search-f.svg", "Github", StandaloneSampleIcons::class.java)
                }
            }

            Tooltip({
                Text("Open Jewel Github repository")
            }) {
                IconButton({
                    Desktop.getDesktop()
                        .browse(URI.create("https://github.com/Leslie-DD/PasswdDesktop")) // https://github.com/JetBrains/jewel
                }, Modifier.size(40.dp).padding(5.dp)) {
                    Icon("icons/github@20x20.svg", "Github", StandaloneSampleIcons::class.java)
                }
            }
        }
    }
}

@Composable
private fun ExportPasswdsFileIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Default.SaveAs,
            contentDescription = "save data to disk",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text("save data to disk")
    }
}

@Composable
fun TitleBarColors.Companion.customLight(
    backgroundColor: Color = IntUiDarkTheme.colors.grey(12),
    inactiveBackground: Color = IntUiDarkTheme.colors.grey(10),
    fullscreenControlButtonsBackground: Color = Color(0xFF575A5C),
    contentColor: Color = IntUiDarkTheme.colors.grey(2),
    borderColor: Color = IntUiDarkTheme.colors.grey(9),
    titlePaneButtonHoveredBackground: Color = Color(0x1AFFFFFF),
    titlePaneButtonPressedBackground: Color = titlePaneButtonHoveredBackground,
    titlePaneCloseButtonHoveredBackground: Color = Color(0xFFE81123),
    titlePaneCloseButtonPressedBackground: Color = Color(0xFFF1707A),
    iconButtonHoveredBackground: Color = IntUiLightTheme.colors.grey(10),
    iconButtonPressedBackground: Color = IntUiLightTheme.colors.grey(10),
    dropdownHoveredBackground: Color = Color(0x1AFFFFFF),
    dropdownPressedBackground: Color = dropdownHoveredBackground,
): TitleBarColors =
    TitleBarColors(
        background = backgroundColor,
        inactiveBackground = inactiveBackground,
        content = contentColor,
        border = borderColor,
        fullscreenControlButtonsBackground = fullscreenControlButtonsBackground,
        titlePaneButtonHoveredBackground = titlePaneButtonHoveredBackground,
        titlePaneButtonPressedBackground = titlePaneButtonPressedBackground,
        titlePaneCloseButtonHoveredBackground = titlePaneCloseButtonHoveredBackground,
        titlePaneCloseButtonPressedBackground = titlePaneCloseButtonPressedBackground,
        iconButtonHoveredBackground = iconButtonHoveredBackground,
        iconButtonPressedBackground = iconButtonPressedBackground,
        dropdownHoveredBackground = dropdownHoveredBackground,
        dropdownPressedBackground = dropdownPressedBackground,
    )

@Composable
fun SaveFileDialog(
    parent: Frame? = null,
    allowedExtensions: List<String>,
    allowMultiSelection: Boolean = false,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory + file)
                }
            }
        }.apply {
            isMultipleMode = allowMultiSelection
            // windows
            file = allowedExtensions.joinToString(";") { "*$it" }
            // linux
            filenameFilter = JsonFileFilter(allowedExtensions)
//            filenameFilter = FolderFilter()
        }
    },
    dispose = FileDialog::dispose
)

internal class JsonFileFilter(
    private val allowedExtensions: List<String>
) : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return allowedExtensions.any {
            name.endsWith(it)
        }
    }
}

internal class FolderFilter : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return File(dir, name).isDirectory
    }
}