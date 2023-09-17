package ui.content

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import entity.Passwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.PasswdsViewModel
import model.UiAction
import model.uieffect.DialogUiEffect
import ui.AddPasswdDialog
import ui.DeletePasswdConfirmDialog
import ui.NoRippleInteractionSource


/**
 * 密码 List（显示指定分组下的所有密码，每条 item 只显示 title 和 username）
 */
@Composable
fun PasswdList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope
) {
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
        SearchBox(coroutineScope) {
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
                        onCloseClick = {
                            isNewPasswdDialogOpened.value = false
                        }
                    ) { title, username, password, link, comment ->
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
                DeletePasswdConfirmDialog {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBox(
    coroutineScope: CoroutineScope,
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

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            focusRequester.requestFocus()
        }
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