package passwds.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    Row(
        modifier = modifier
            .padding(end = 10.dp)
            .background(color = MaterialTheme.colorScheme.primaryContainer)
    ) {
        GroupList(viewModel = viewModel, modifier = Modifier.width(250.dp))
        RowSpacer()
        PasswdsAndDetailWrapper(viewModel = viewModel)
    }
}

/**
 * 密码分组 List
 */
@Composable
fun GroupList(
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
        modifier = modifier.fillMaxSize()
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
                    GroupItem(
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
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
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
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
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
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
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
fun PasswdsAndDetailWrapper(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            PasswdList(viewModel = viewModel, modifier = Modifier.width(250.dp))
            RowSpacer()
            PasswdDetailWrapper(viewModel = viewModel)
        }
    }
}

/**
 * 密码 List（显示指定分组下的所有密码，每条 item 只显示 title 和 username）
 */
@Composable
fun PasswdList(
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

        Column(
            modifier = Modifier.weight(1f)
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
                        PasswdItem(
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
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
fun PasswdDetailWrapper(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxSize()
    ) {
        val passwdUiState = viewModel.passwdUiState.collectAsState().value
        passwdUiState.selectPasswd?.let {
            val isEditDialogOpen = remember { mutableStateOf(false) }
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PasswdDetails(it)
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
private fun PasswdDetails(
    passwd: Passwd,
    editEnable: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DetailTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "title",
            leadingIcon = Icons.Default.Title,
            value = passwd.title ?: ""
        )
        DetailTextField(
            label = "username",
            leadingIcon = Icons.Default.People,
            value = passwd.usernameString ?: ""
        )
        var passwordVisible by remember { mutableStateOf(false) }
        DetailTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "password",
            leadingIcon = Icons.Default.Lock,
            value = passwd.passwordString ?: "",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Box(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            }
        )

        DetailTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "link",
            leadingIcon = Icons.Default.Link,
            value = passwd.link ?: ""
        )
        OutlinedTextField(
            enabled = false,
            modifier = Modifier.fillMaxSize().align(Alignment.Start),
            label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            value = passwd.comment ?: "",
            onValueChange = { },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledLabelColor = MaterialTheme.colorScheme.outline,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTextField(
    enabled: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String,
    leadingIcon: ImageVector,
    value: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        enabled = enabled,
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        value = value,
        maxLines = 1,
        onValueChange = { },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.outline,
        )
    )
}

@Composable
fun GroupItem(
    group: Group,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    TextButton(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(end = 10.dp),
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

@Composable
fun PasswdItem(
    passwd: Passwd,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20),
        interactionSource = remember { NoRippleInteractionSource() },
        onClick = { onClick(passwd.id) },
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                text = passwd.title ?: "",
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(0.1f))
            if (!passwd.usernameString.isNullOrBlank()) {
                Text(
                    text = passwd.usernameString!!,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
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
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                text.value = ""
            }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20),
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
        )
    }
}

@Composable
private fun RowSpacer() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(16.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)
    )
}