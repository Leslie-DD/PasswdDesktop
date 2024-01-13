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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderState
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import entity.IDragAndDrop
import entity.Passwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.action.PasswdAction
import model.action.UiAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UiConfigViewModel
import ui.common.AddPasswdDialog
import ui.common.CustomOutlinedTextField
import ui.common.DeletePasswdConfirmDialog
import ui.common.defaultIconButtonColors
import ui.toolbar.NoRippleInteractionSource

/**
 * 密码 List（显示指定分组下的所有密码，每条 item 只显示 title 和 username）
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun PasswdList(
    passwdsViewModel: PasswdsViewModel,
    uiConfigViewModel: UiConfigViewModel,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    reorderState: ReorderState<IDragAndDrop>
) {
    val listState = rememberLazyListState()
    val dialogUiState = passwdsViewModel.dialogUiState.collectAsState().value

    val isNewPasswdDialogOpened = remember { mutableStateOf(false) }
    val isDeletePasswdConfirmDialogOpened = remember { mutableStateOf(false) }
    when (dialogUiState.effect) {
        is DialogUiEffect.NewPasswdResult -> {
            isNewPasswdDialogOpened.value = false
            passwdsViewModel.onAction(PasswdAction.ClearEffect)

            coroutineScope.launch {
                val size = passwdsViewModel.passwdUiState.value.groupPasswds.size
                if (size > 0) {
                    listState.animateScrollToItem(index = size - 1)
                }
            }
        }

        is DialogUiEffect.DeletePasswdResult -> {
            isDeletePasswdConfirmDialogOpened.value = false
            passwdsViewModel.onAction(PasswdAction.ClearEffect)
        }

        else -> {}
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val searchFocus by uiConfigViewModel.searchFocus.collectAsState()
            if (searchFocus) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 10.dp, end = 20.dp)) {
                    SearchBox(passwdsViewModel, uiConfigViewModel)
                }
            }

            val passwdUiState = passwdsViewModel.passwdUiState.collectAsState().value
            val selectPasswd = passwdUiState.selectPasswd
            Row(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = modifier.weight(1f).padding(10.dp)
                        .dropTarget(
                            key = "listTwo",
                            state = reorderState.dndState,
                            dropAnimationEnabled = false,
                        ),
                    state = listState
                ) {
                    items(passwdUiState.groupPasswds, { it }) { passwd ->
                        ReorderableItem(
                            state = reorderState,
                            key = passwd,
                            data = passwd,
                            zIndex = 1f,
                            onDragEnter = { state ->
                                passwdsViewModel.onPasswdListItemDragEnter(passwd, state.data)
                            },
                            draggableContent = {
                                PasswdItem(
                                    modifier = Modifier.shadow(elevation = 20.dp),
                                    passwd = passwd,
                                    isSelected = passwd.id == selectPasswd?.id
                                )
                            },
                            modifier = Modifier
                        ) {
                            PasswdItem(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isDragging) 0f else 1f
                                    },
                                passwd = passwd,
                                isSelected = passwd.id == selectPasswd?.id
                            ) {
                                passwdsViewModel.onAction(PasswdAction.ShowPasswd(passwdId = it))
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

                val groupUiState = passwdsViewModel.passwdUiState.collectAsState().value
                IconButton(
                    enabled = groupUiState.selectGroup != null,
                    colors = defaultIconButtonColors(),
                    onClick = {
                        isNewPasswdDialogOpened.value = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }

                if (isNewPasswdDialogOpened.value) {
                    val selectGroupId = groupUiState.selectGroup?.id
                    AddPasswdDialog(
                        onCloseClick = {
                            isNewPasswdDialogOpened.value = false
                        }
                    ) { title, username, password, link, comment ->
                        selectGroupId?.let {
                            passwdsViewModel.onAction(
                                PasswdAction.NewPasswd(
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
                    enabled = selectPasswd != null,
                    colors = defaultIconButtonColors(),
                    onClick = {
                        isDeletePasswdConfirmDialogOpened.value = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }

            }

            if (isDeletePasswdConfirmDialogOpened.value) {
                DeletePasswdConfirmDialog {
                    if (it) {
                        passwdsViewModel.onAction(PasswdAction.DeletePasswd)
                    } else {
                        isDeletePasswdConfirmDialogOpened.value = false
                    }
                }
            }
        }
    }
}

@Composable
fun PasswdItem(
    modifier: Modifier = Modifier,
    passwd: Passwd,
    isSelected: Boolean,
    onClick: (Int) -> Unit = {}
) {
    TextButton(
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun SearchBox(
    passwdsViewModel: PasswdsViewModel,
    uiConfigViewModel: UiConfigViewModel
) {
    val searchFocus by uiConfigViewModel.searchFocus.collectAsState()
    CustomOutlinedTextField(
        requestFocus = true,
        onFocusChanged = {
            if (it) {
                uiConfigViewModel.onAction(UiAction.FocusOnSearch(true))
            }
        },
        modifier = Modifier.height(32.dp).fillMaxWidth().onPreviewKeyEvent {
            when {
                (it.isCtrlPressed && it.key == Key.F && it.type == KeyEventType.KeyDown) -> {
                    if (searchFocus) {
                        uiConfigViewModel.onAction(UiAction.FocusOnSearch(false))
                    }
                    true
                }

                else -> false
            }
        },
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
        onValueChange = {
            if (it.isNotBlank()) {
                passwdsViewModel.onAction(PasswdAction.SearchPasswds(it))
            }
        }
    )
    Spacer(modifier = Modifier.width(10.dp))
}