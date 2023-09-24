package ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.viewmodel.PasswdsViewModel
import ui.RowSpacer

/**
 * 密码界面主要内容的显示区域
 */
@Composable
fun ContentScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
    ) {
        ToolBar(viewModel = viewModel)
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 10.dp)
        ) {
            GroupList(viewModel, Modifier.width(250.dp), coroutineScope)
            RowSpacer()
            PasswdList(viewModel, Modifier.width(250.dp), coroutineScope)
            RowSpacer()
            PasswdDetailWrapper(viewModel = viewModel)
        }
    }
}