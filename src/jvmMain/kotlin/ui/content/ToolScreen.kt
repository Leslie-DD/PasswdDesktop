package ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AwtWindow
import model.UiAction
import model.viewmodel.PasswdsViewModel
import ui.CustomOutlinedTextField
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter


@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    viewModel: PasswdsViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExportPasswdsFileIcon(viewModel)
            SearchBox(viewModel, modifier)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}

@Composable
private fun SearchBox(viewModel: PasswdsViewModel, modifier: Modifier) {
    CustomOutlinedTextField(
        modifier = Modifier.height(32.dp),
        leadingIcon = {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        placeholder = {
            Text(
                text = "Search",
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
        },
        onValueChange = { viewModel.onAction(UiAction.SearchPasswds(it)) }
    )
    Spacer(modifier = modifier.fillMaxHeight().width(10.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExportPasswdsFileIcon(
    viewModel: PasswdsViewModel
) {
    var isFileChooserOpen by remember { mutableStateOf(false) }
    TooltipArea(
        tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "export passwds to file",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
        },
        modifier = Modifier.padding(start = 40.dp),
        delayMillis = 600, // in milliseconds
        tooltipPlacement = TooltipPlacement.CursorPoint(
            alignment = Alignment.BottomEnd,
            offset = DpOffset.Zero // tooltip offset
        )
    ) {
        IconButton(
            onClick = { isFileChooserOpen = true }
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.SaveAs,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (isFileChooserOpen) {
            SaveFileDialog(
                allowedExtensions = arrayListOf(".json"),
                onCloseRequest = {
                    isFileChooserOpen = false
                    println("Result $it")
                    it?.let {
                        viewModel.onAction(UiAction.ExportPasswdsToFile(it))
                    }
                }
            )
        }
    }
}

@Composable
private fun SaveFileDialog(
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