package ui.passwd

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderState
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import entity.Group
import entity.IDragAndDrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.UiAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import ui.common.AddGroupDialog
import ui.common.DeleteGroupConfirmDialog
import ui.common.EditGroupDialog
import ui.toolbar.NoRippleInteractionSource


/**
 * 密码分组 List
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun GroupList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    reorderState: ReorderState<IDragAndDrop>
) {
    val listState = rememberLazyListState()
    val dialogUiState = viewModel.dialogUiState.collectAsState().value

    val isNewGroupDialogOpen = remember { mutableStateOf(false) }
    val isUpdateGroupDialogOpen = remember { mutableStateOf(false) }
    val isDeleteGroupConfirmDialogOpen = remember { mutableStateOf(false) }

    when (dialogUiState.effect) {
        is DialogUiEffect.NewGroupResult -> {
            isNewGroupDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)

            coroutineScope.launch {
                val size = viewModel.passwdUiState.value.groups.size
                if (size > 0) {
                    listState.animateScrollToItem(index = size - 1)
                }
            }
        }

        is DialogUiEffect.UpdateGroupResult -> {
            isUpdateGroupDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)
        }

        is DialogUiEffect.DeleteGroupResult -> {
            isDeleteGroupConfirmDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)
        }

        else -> {}
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        val groupUiState = viewModel.passwdUiState.collectAsState().value
        val selectGroup = groupUiState.selectGroup
        Row(
            modifier = Modifier.weight(1f).padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp)
                    .dropTarget(
                        key = "listOne",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
                            viewModel.onGroupListDragEnter(state.data)
                        },
                    ),
                state = listState,
            ) {
                items(groupUiState.groups, { it }) { group ->
                    ReorderableItem(
                        state = reorderState,
                        key = group,
                        data = group,
                        zIndex = 1f,
                        onDragEnter = { state ->
                            viewModel.onGroupListItemDragEnter(group, state.data)
                        },
                        draggableContent = {
                            GroupItem(
                                modifier = Modifier.shadow(elevation = 20.dp),
                                group = group,
                                isSelected = group.id == selectGroup?.id,
                            )
                        }
                    ) {
                        GroupItem(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                },
                            group = group,
                            isSelected = group.id == selectGroup?.id,
                        ) {
                            viewModel.onAction(UiAction.ShowGroupPasswds(groupId = it))
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = listState
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { isNewGroupDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }

            if (isNewGroupDialogOpen.value) {
                AddGroupDialog(
                    onCloseClick = {
                        isNewGroupDialogOpen.value = false
                    }
                ) { groupName, groupComment ->
                    viewModel.onAction(UiAction.NewGroup(groupName, groupComment))
                }
            }

            IconButton(
                enabled = selectGroup != null,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { isUpdateGroupDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            if (isUpdateGroupDialogOpen.value) {
                selectGroup?.let {
                    EditGroupDialog(
                        groupName = it.groupName ?: "",
                        groupComment = it.groupComment ?: "",
                        onCloseClick = {
                            isUpdateGroupDialogOpen.value = false
                        }
                    ) { groupName, groupComment ->
                        viewModel.onAction(UiAction.UpdateGroup(groupName, groupComment))
                    }
                }
            }

            IconButton(
                enabled = selectGroup != null,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { isDeleteGroupConfirmDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }

            if (isDeleteGroupConfirmDialogOpen.value) {
                DeleteGroupConfirmDialog { delete ->
                    if (delete) {
                        viewModel.onAction(UiAction.DeleteGroup)
                    } else {
                        isDeleteGroupConfirmDialogOpen.value = false
                    }
                }
            }
        }
    }
}

@Composable
fun GroupItem(
    modifier: Modifier = Modifier,
    group: Group,
    isSelected: Boolean,
    onClick: (Int) -> Unit = {}
) {
    TextButton(
        modifier = modifier.fillMaxWidth().height(40.dp).padding(end = 10.dp),
        shape = RoundedCornerShape(20),
        interactionSource = remember { NoRippleInteractionSource() },
        onClick = { onClick(group.id) },
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    ) {
        Spacer(modifier = Modifier.width(15.dp).fillMaxHeight())
        Text(
            text = group.groupName ?: "",
            fontSize = 14.sp, maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.weight(0.6f))
    }
}