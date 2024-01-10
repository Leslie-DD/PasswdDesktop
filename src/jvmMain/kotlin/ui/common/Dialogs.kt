package ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import entity.Passwd

@Composable
fun AddPasswdDialog(
    onCloseClick: () -> Unit,
    onAddClick: (String, String, String, String, String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val comment = remember { mutableStateOf("") }
    val link = remember { mutableStateOf("") }

    GlobalDialog(
        title = "Add a passwd",
        onCloseClick = { onCloseClick() },
        size = DpSize(600.dp, 600.dp),
        onConfirmClick = {
            onAddClick(title.value, username.value, password.value, link.value, comment.value)
        },
        confirmString = "Add"
    ) {
        Column(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
        ) {
            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                labelValue = "title",
                leadingIconImageVector = Icons.Default.Title,
                value = title.value,
                onValueChange = { title.value = it },
            )
            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                labelValue = "username",
                leadingIconImageVector = Icons.Default.People,
                value = username.value,
                onValueChange = { username.value = it }
            )
            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                labelValue = "password",
                leadingIconImageVector = Icons.Default.Lock,
                value = password.value,
                onValueChange = { password.value = it }
            )
            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                labelValue = "link",
                leadingIconImageVector = Icons.Default.Link,
                value = link.value,
                onValueChange = { link.value = it }
            )
            EnabledOutlinedTextField(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Start),
                value = comment.value,
                labelValue = "comment",
                maxLines = 10,
                singleLine = false,
            ) { comment.value = it }
        }
    }
}

@Composable
fun DeletePasswdConfirmDialog(
    onDeleteConfirmOrCancelClick: (Boolean) -> Unit
) {
    GlobalDialog(
        title = "Delete the Passwd",
        onCloseClick = { onDeleteConfirmOrCancelClick(false) },
        onConfirmClick = { onDeleteConfirmOrCancelClick(true) },
        onCancelClick = { onDeleteConfirmOrCancelClick(false) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = "删除该密码将不可恢复，确定删除吗？",
                fontSize = 14.sp
            )
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
    GlobalDialog(
        title = "Add a Group",
        onCloseClick = { onCloseClick() },
        onConfirmClick = { onAddClick(groupName.value, commentName.value) },
        confirmString = "Add"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = groupName.value,
                labelValue = "Group Name",
                leadingIconImageVector = Icons.Outlined.Group
            ) {
                groupName.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))

            EnabledOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = commentName.value,
                labelValue = "Group Comment",
                leadingIconImageVector = Icons.Outlined.Comment
            ) {
                commentName.value = it
            }
        }
    }

}

@Composable
fun EditGroupDialog(
    groupName: String,
    groupComment: String,
    onCloseClick: () -> Unit,
    onUpdateClick: (String, String) -> Unit
) {
    val groupNameState = remember { mutableStateOf(groupName) }
    val groupCommentState = remember { mutableStateOf(groupComment) }
    GlobalDialog(
        title = "Edit the Group",
        onCloseClick = { onCloseClick() },
        onConfirmClick = { onUpdateClick(groupNameState.value, groupCommentState.value) },
        confirmString = "Update"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnabledOutlinedTextField(
                value = groupNameState.value,
                labelValue = "Group Name",
                leadingIconImageVector = Icons.Outlined.Group
            ) {
                groupNameState.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))

            EnabledOutlinedTextField(
                value = groupCommentState.value,
                labelValue = "Group Comment",
                leadingIconImageVector = Icons.Outlined.Comment
            ) {
                groupCommentState.value = it
            }
        }
    }

}

@Composable
fun DeleteGroupConfirmDialog(
    onDeleteConfirmOrCancelClick: (Boolean) -> Unit
) {
    GlobalDialog(
        title = "Delete the Group",
        onCloseClick = { onDeleteConfirmOrCancelClick(false) },
        onConfirmClick = { onDeleteConfirmOrCancelClick(true) },
        onCancelClick = { onDeleteConfirmOrCancelClick(false) }
    ) {
        val enable = remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = "删除该分组，该分组下所有密码将会被删除并且不可恢复，确定删除吗？",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TipsDialog(
    title: String = "Uh-oh!",
    infoLabel: String = "label",
    info: String? = null,
    warn: String? = null,
    buttonValue: String = "Got it",
    onCloseClick: () -> Unit,
) {
    GlobalDialog(
        title = title,
        size = DpSize(600.dp, 400.dp),
        onCloseClick = { onCloseClick() },
        onConfirmClick = { onCloseClick() },
        confirmString = buttonValue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            info?.let {
                ReadableOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = infoLabel,
                    leadingIcon = Icons.Default.Key,
                    value = info,
                    trailingIcon = {
                        Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                            IconButton(
                                colors = defaultIconButtonColors(),
                                onClick = {
                                    info.copyToClipboard()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "copy password to clipboard"
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            warn?.let {
                Text(
                    text = warn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


