package passwds.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.launch
import passwds.entity.Group
import passwds.entity.Passwd
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiEffect

/**
 * 密码界面主要内容的显示区域
 */
@Composable
fun PasswdsScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = Modifier.fillMaxSize()) {
        PasswdGroupList(viewModel = viewModel, modifier = Modifier.width(250.dp))
        Divider(
            modifier = Modifier
                .padding(0.dp, 10.dp)
                .fillMaxHeight()
                .width(1.dp)
        )
        PasswdItemsContent(viewModel = viewModel)
    }
}

/**
 * 密码分组 List
 */
@Composable
fun PasswdGroupList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val effect = viewModel.uiState.effect

    val listState = rememberLazyListState()
    val groups = viewModel.translateUiState.groups

    val isNewGroupDialogOpen = remember { mutableStateOf(false) }
    val onNewGroupDialogClose = {
        isNewGroupDialogOpen.value = false
        viewModel.onAction(UiAction.ClearEffect)
    }

    val isDeleteGroupConfirmDialogOpen = remember { mutableStateOf(false) }


    when (effect) {
        is UiEffect.NewGroupResult -> {
            onNewGroupDialogClose()
            coroutineScope.launch {
                listState.animateScrollToItem(index = groups.size - 1)
            }
        }

        is UiEffect.DeleteGroupResult -> {
            isDeleteGroupConfirmDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)
        }

        else -> {}
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(10.dp),
            state = listState
        ) {
            items(groups) { group ->
                GroupCard(
                    group = group,
                    isSelected = group.id == viewModel.uiState.selectGroup?.id
                ) {
                    viewModel.onAction(UiAction.ShowGroupPasswds(groupId = it))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { isNewGroupDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }

            if (isNewGroupDialogOpen.value) {
                AddGroupDialog(
                    onCloseClick = onNewGroupDialogClose,
                    onAddClick = { groupName, groupComment ->
                        viewModel.onAction(UiAction.NewGroup(groupName, groupComment))
                    }
                )
            }

            IconButton(
                enabled = viewModel.uiState.selectGroup != null,
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
fun AddGroupDialog(
    onCloseClick: () -> Unit,
    onAddClick: (String, String) -> Unit
) {
    val groupName = remember { mutableStateOf("") }
    val commentName = remember { mutableStateOf("") }
    val enable = remember { mutableStateOf(true) }
    Dialog(
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Add a Group")
            Spacer(modifier = Modifier.height(30.dp))

            EditTextBox(
                value = groupName.value,
                labelValue = "Group Name",
                imageVector = Icons.Outlined.Group
            ) {
                groupName.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))

            EditTextBox(
                value = commentName.value,
                labelValue = "Group Comment",
                imageVector = Icons.Outlined.Comment
            ) {
                commentName.value = it
            }

            Button(
                enabled = enable.value,
                onClick = {
                    enable.value = false
                    onAddClick(groupName.value, commentName.value)
                }
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
fun DeleteGroupConfirmDialog(
    onDeleteConfirmOrCancelClick: (Boolean) -> Unit
) {
    Dialog(
        onCloseRequest = { onDeleteConfirmOrCancelClick(false) },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        val enable = remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete the Group",
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                text = "删除该分组，该分组下所有密码将会被删除并且不可恢复，确定删除吗？",
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = enable.value,
                    onClick = {
                        enable.value = false
                        onDeleteConfirmOrCancelClick(true)
                    }
                ) {
                    Text(text = "Confirm")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    enabled = enable.value,
                    onClick = {
                        enable.value = false
                        onDeleteConfirmOrCancelClick(false)
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

/**
 * 包含密码 List 和密码详情两部分
 */
@Composable
fun PasswdItemsContent(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            PasswdItemsList(viewModel = viewModel, modifier = Modifier.width(250.dp))
            Divider(
                modifier = Modifier
                    .padding(0.dp, 10.dp)
                    .fillMaxHeight()
                    .width(1.dp)
            )
            PasswdDetailScreen(viewModel = viewModel)
        }
    }
}

/**
 * 密码 List（显示指定分组下的所有密码，每条 item 只显示 title 和 username）
 */
@Composable
fun PasswdItemsList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            items(viewModel.translateUiState.groupPasswds) { passwd ->
                PasswdCard(
                    passwd = passwd,
                    isSelected = passwd.id == viewModel.uiState.selectPasswd?.id
                ) {
                    viewModel.onAction(UiAction.ShowPasswd(passwdId = it))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }

            IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

/**
 * 密码详情
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswdDetailScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    val passwd = viewModel.uiState.selectPasswd
    Box(modifier.fillMaxSize().padding(16.dp)) {
        passwd?.let {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    value = passwd.title ?: "",
                    maxLines = 1,
                    onValueChange = { },
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    value = passwd.usernameString ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    value = passwd.passwordString ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("link", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    value = passwd.link ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize().align(Alignment.Start),
                    label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    value = passwd.comment ?: "",
                    onValueChange = { }
                )
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isSelected
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CutCornerShape(bottomEndPercent = 40, topEndPercent = 60)
                    )
                )
            }
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { NoRippleInteractionSource() },
            onClick = { onClick(group.id) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Spacer(modifier = Modifier.width(15.dp).fillMaxHeight())
            Text(text = group.groupName ?: "", fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.weight(0.6f))
        }
    }
}

@Composable
fun PasswdCard(
    passwd: Passwd,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isSelected
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CutCornerShape(bottomEndPercent = 40, topEndPercent = 60)
                    )
                )
            }
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { NoRippleInteractionSource() },
            onClick = { onClick(passwd.id) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(0.1f))
                Text(text = passwd.title ?: "", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(0.1f))
                Text(
                    text = passwd.usernameString ?: "",
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}