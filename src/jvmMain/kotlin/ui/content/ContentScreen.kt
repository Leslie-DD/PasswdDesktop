package ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.UiAction
import model.viewmodel.PasswdsViewModel
import ui.CustomOutlinedTextField
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
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer)
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

@Composable
private fun ToolBar(
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

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}