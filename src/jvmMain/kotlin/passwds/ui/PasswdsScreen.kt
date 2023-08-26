package passwds.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import passwds.entity.Group
import passwds.entity.Passwd
import passwds.model.DialogUiEffect
import passwds.model.PasswdsViewModel
import passwds.model.UiAction

/**
 * 密码界面主要内容的显示区域
 */
@Composable
fun PasswdsScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(end = 10.dp, top = 10.dp, bottom = 10.dp)) {
        PasswdGroupList(viewModel = viewModel, modifier = Modifier.width(250.dp))
        Spacer(modifier = Modifier.fillMaxHeight().width(10.dp))
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
    val dialogUiState = viewModel.dialogUiState.collectAsState().value

    val listState = rememberLazyListState()
    val groupState = viewModel.groupUiState.collectAsState().value
    val groups = groupState.groups

    val isNewGroupDialogOpen = remember { mutableStateOf(false) }
    val isUpdateGroupDialogOpen = remember { mutableStateOf(false) }
    val isDeleteGroupConfirmDialogOpen = remember { mutableStateOf(false) }


    when (dialogUiState.effect) {
        is DialogUiEffect.NewGroupResult -> {
            isNewGroupDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)

            coroutineScope.launch {
                val size = groups.size
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
        modifier = modifier.fillMaxSize().background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp)
        )
    ) {
        val groupUiState = viewModel.groupUiState.collectAsState().value
        val selectGroup = groupUiState.selectGroup
        Row(
            modifier = Modifier.weight(1f).padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(10.dp),
                state = listState
            ) {
                items(groups) { group ->
                    GroupCard(
                        group = group,
                        isSelected = group.id == selectGroup?.id
                    ) {
                        viewModel.onAction(UiAction.ShowGroupPasswds(groupId = it))
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
                onClick = { isNewGroupDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }

            val theme by viewModel.theme.collectAsState()
            if (isNewGroupDialogOpen.value) {
                AddGroupDialog(
                    onCloseClick = {
                        isNewGroupDialogOpen.value = false
                    },
                    onAddClick = { groupName, groupComment ->
                        viewModel.onAction(UiAction.NewGroup(groupName, groupComment))
                    },
                    theme = theme
                )
            }

            IconButton(
                enabled = selectGroup != null,
                onClick = { isUpdateGroupDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            if (isUpdateGroupDialogOpen.value) {
                selectGroup?.let {
                    UpdateGroupDialog(
                        groupName = it.groupName ?: "",
                        groupComment = it.groupComment ?: "",
                        onCloseClick = {
                            isUpdateGroupDialogOpen.value = false
                        },
                        onUpdateClick = { groupName, groupComment ->
                            viewModel.onAction(UiAction.UpdateGroup(groupName, groupComment))
                        },
                        theme = theme
                    )
                }
            }

            IconButton(
                enabled = selectGroup != null,
                onClick = { isDeleteGroupConfirmDialogOpen.value = true }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }

            if (isDeleteGroupConfirmDialogOpen.value) {
                DeleteGroupConfirmDialog(theme = theme) { delete ->
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
            Spacer(modifier = Modifier.fillMaxHeight().width(10.dp))
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
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val dialogUiState = viewModel.dialogUiState.collectAsState().value

    val isNewPasswdDialogOpened = remember { mutableStateOf(false) }
    val isDeletePasswdConfirmDialogOpened = remember { mutableStateOf(false) }
    when (dialogUiState.effect) {
        is DialogUiEffect.NewPasswdResult -> {
            isNewPasswdDialogOpened.value = false
            viewModel.onAction(UiAction.ClearEffect)

            coroutineScope.launch {
                val size = viewModel.passwdUiState.value.groupPasswds.size
                if (size > 0) {
                    listState.animateScrollToItem(index = size - 1)
                }
            }
        }

        is DialogUiEffect.DeletePasswdResult -> {
            isDeletePasswdConfirmDialogOpened.value = false
            viewModel.onAction(UiAction.ClearEffect)
        }

        else -> {}
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SearchBox {
            viewModel.onAction(UiAction.SearchPasswds(it))
        }
        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        Column(
            modifier = Modifier.weight(1f).background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            )
        ) {
            val passwdUiState = viewModel.passwdUiState.collectAsState().value
            Row(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = modifier.weight(1f).padding(10.dp),
                    state = listState
                ) {
                    items(passwdUiState.groupPasswds) { passwd ->
                        PasswdCard(
                            passwd = passwd,
                            isSelected = passwd.id == passwdUiState.selectPasswd?.id
                        ) {
                            viewModel.onAction(UiAction.ShowPasswd(passwdId = it))
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

                val groupUiState = viewModel.groupUiState.collectAsState().value
                IconButton(
                    enabled = groupUiState.selectGroup != null,
                    onClick = {
                        isNewPasswdDialogOpened.value = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }

                if (isNewPasswdDialogOpened.value) {
                    val selectGroupId = groupUiState.selectGroup?.id
                    val theme by viewModel.theme.collectAsState()
                    AddPasswdDialog(
                        theme = theme,
                        onCloseClick = {
                            isNewPasswdDialogOpened.value = false
                        },
                        onAddClick = { title, username, password, link, comment ->
                            selectGroupId?.let {
                                viewModel.onAction(
                                    UiAction.NewPasswd(
                                        groupId = it,
                                        title = title,
                                        usernameString = username,
                                        passwordString = password,
                                        link = link,
                                        comment = comment
                                    )
                                )
                            }
                        }
                    )
                }

                IconButton(
                    enabled = passwdUiState.selectPasswd != null,
                    onClick = {
                        isDeletePasswdConfirmDialogOpened.value = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }

            }

            if (isDeletePasswdConfirmDialogOpened.value) {
                val theme by viewModel.theme.collectAsState()
                DeletePasswdConfirmDialog(theme = theme) {
                    if (it) {
                        viewModel.onAction(UiAction.DeletePasswd)
                    } else {
                        isDeletePasswdConfirmDialogOpened.value = false
                    }
                }
            }
        }
    }
}

/**
 * 密码详情
 */
@Composable
fun PasswdDetailScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxSize().background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp)
        )
    ) {
        val passwdUiState = viewModel.passwdUiState.collectAsState().value
        passwdUiState.selectPasswd?.let {
            val isEditDialogOpen = remember { mutableStateOf(false) }
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PasswdDetailBox(it)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            isEditDialogOpen.value = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    }

                }
            }


            val dialogUiState = viewModel.dialogUiState.collectAsState().value
            when (dialogUiState.effect) {
                is DialogUiEffect.UpdatePasswdResult -> {
                    isEditDialogOpen.value = false
                    viewModel.onAction(UiAction.ClearEffect)
                }

                else -> {}
            }

            if (isEditDialogOpen.value) {
                val theme by viewModel.theme.collectAsState()
                PasswdDetailEditDialog(
                    theme = theme,
                    onEditConfirmClick = { passwd: Passwd ->
                        viewModel.onAction(UiAction.UpdatePasswd(passwd))
                    },
                    onCloseClick = { isEditDialogOpen.value = false },
                    passwd = it
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswdDetailBox(
    passwd: Passwd,
    editEnable: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            enabled = editEnable,
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
            enabled = editEnable,
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
            enabled = editEnable,
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
            enabled = editEnable,
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
            enabled = editEnable,
            modifier = Modifier.fillMaxSize().align(Alignment.Start),
            label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            value = passwd.comment ?: "",
            onValueChange = { }
        )
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
            Text(
                text = group.groupName ?: "",
                fontSize = 14.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        interactionSource = remember { NoRippleInteractionSource() },
        onClick = { onClick(passwd.id) },
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        ),
        shape = RectangleShape
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                text = passwd.title ?: "",
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.weight(0.1f))
            if (!passwd.usernameString.isNullOrBlank()) {
                Text(
                    text = passwd.usernameString!!,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBox(
    onSearch: (String) -> Unit
) {
    val text = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                text.value = ""
//                onSearch(text.value)
            }
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            maxLines = 1,
            placeholder = {
                Text(
                    text = "Search",
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            value = text.value,
            onValueChange = {
                text.value = it
                onSearch(text.value)
            },
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
        )
    }
}