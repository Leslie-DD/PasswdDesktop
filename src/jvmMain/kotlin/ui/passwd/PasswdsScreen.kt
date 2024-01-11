package ui.passwd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import entity.IDragAndDrop
import model.viewmodel.ConfigViewModel
import model.viewmodel.PasswdsViewModel
import ui.common.RowSpacer
import ui.toolbar.SideMenuBar

/**
 * 密码界面主要内容的显示区域
 */
@Composable
fun PasswdsScreen(
    passwdsViewModel: PasswdsViewModel,
    configViewModel: ConfigViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SideMenuBar(passwdsViewModel, configViewModel)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer)
        ) {
            val reorderState = rememberReorderState<IDragAndDrop>()
            val coroutineScope = rememberCoroutineScope()
            ReorderContainer(state = reorderState) {
                Row(modifier = Modifier) {
                    GroupList(passwdsViewModel, Modifier.width(250.dp), coroutineScope, reorderState)
                    RowSpacer()
                    PasswdList(passwdsViewModel, Modifier.width(250.dp), coroutineScope, reorderState)
                    RowSpacer()
                }
            }
            PasswdDetailScreen(viewModel = passwdsViewModel)
        }
    }

}