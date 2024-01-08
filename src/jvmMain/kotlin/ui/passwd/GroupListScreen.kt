package ui.passwd

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import entity.Group
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.UiAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import ui.common.AddGroupDialog
import ui.common.DeleteGroupConfirmDialog
import ui.common.EditGroupDialog
import ui.toolbar.NoRippleInteractionSource


/**
 * 密码分组 List
 */
@Composable
fun GroupList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope
) {
    val dialogUiState = viewModel.dialogUiState.collectAsState().value
    val groupUiState = viewModel.passwdUiState.collectAsState().value

    val reorderableGroups = viewModel.reorderGroupList.collectAsState()
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            viewModel.updateReorderGroupList(reorderableGroups.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            })
        },
        onDragEnd = { _, _ -> viewModel.onAction(UiAction.ReorderGroupDragEnd(reorderableGroups.value)) }
    )

    val isNewGroupDialogOpen = remember { mutableStateOf(false) }
    val isUpdateGroupDialogOpen = remember { mutableStateOf(false) }
    val isDeleteGroupConfirmDialogOpen = remember { mutableStateOf(false) }

    when (dialogUiState.effect) {
        is DialogUiEffect.NewGroupResult -> {
            isNewGroupDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)

            coroutineScope.launch {
                val size = reorderableGroups.value.size
                if (size > 0) {
                    state.listState.animateScrollToItem(index = size - 1)
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
        val selectGroup = groupUiState.selectGroup
        Row(
            modifier = Modifier.weight(1f).padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp)
                    .reorderable(state)
                    .detectReorderAfterLongPress(state),
                state = state.listState,
            ) {
                items(reorderableGroups.value, { it }) { group ->
                    ReorderableItem(
                        reorderableState = state,
                        key = group
                    ) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                        GroupItem(
                            modifier = Modifier.shadow(elevation.value),
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
                    scrollState = state.listState
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