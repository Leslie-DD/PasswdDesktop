package passwds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import model.Theme
import passwds.entity.Passwd


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswdDialog(
    theme: Theme,
    onCloseClick: () -> Unit,
    onAddClick: (String, String, String, String, String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val comment = remember { mutableStateOf("") }
    val link = remember { mutableStateOf("") }
    val enable = remember { mutableStateOf(true) }
    Dialog(
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(400.dp, 800.dp)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = if (theme.isDark) Color.Black else Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    value = title.value,
                    maxLines = 1,
                    onValueChange = { title.value = it },
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    value = username.value,
                    maxLines = 1,
                    onValueChange = { username.value = it }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    value = password.value,
                    maxLines = 1,
                    onValueChange = { password.value = it }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("link", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    value = link.value,
                    maxLines = 1,
                    onValueChange = { link.value = it }
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f).fillMaxWidth().align(Alignment.Start),
                    label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    value = comment.value,
                    onValueChange = { comment.value = it }
                )

                Button(
                    enabled = enable.value,
                    onClick = {
                        enable.value = false
                        onAddClick(title.value, username.value, password.value, link.value, comment.value)
                    }
                ) {
                    Text("Add")
                }
            }
        }

    }
}

@Composable
fun DeletePasswdConfirmDialog(
    theme: Theme,
    onDeleteConfirmOrCancelClick: (Boolean) -> Unit
) {
    Dialog(
        onCloseRequest = { onDeleteConfirmOrCancelClick(false) },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        val enable = remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.fillMaxSize().background(color = if (theme.isDark) Color.Black else Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete the Passwd",
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                text = "删除该密码将不可恢复，确定删除吗？",
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

@Composable
fun AddGroupDialog(
    onCloseClick: () -> Unit,
    onAddClick: (String, String) -> Unit,
    theme: Theme
) {
    val groupName = remember { mutableStateOf("") }
    val commentName = remember { mutableStateOf("") }
    val enable = remember { mutableStateOf(true) }
    Dialog(
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(color = if (theme.isDark) Color.Black else Color.White),
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
fun UpdateGroupDialog(
    groupName: String,
    groupComment: String,
    onCloseClick: () -> Unit,
    onUpdateClick: (String, String) -> Unit,
    theme: Theme
) {
    val groupNameState = remember { mutableStateOf(groupName) }
    val groupCommentState = remember { mutableStateOf(groupComment) }
    val enable = remember { mutableStateOf(true) }
    Dialog(
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(color = if (theme.isDark) Color.Black else Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Update the Group")
            Spacer(modifier = Modifier.height(30.dp))

            EditTextBox(
                value = groupNameState.value,
                labelValue = "Group Name",
                imageVector = Icons.Outlined.Group
            ) {
                groupNameState.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))

            EditTextBox(
                value = groupCommentState.value,
                labelValue = "Group Comment",
                imageVector = Icons.Outlined.Comment
            ) {
                groupCommentState.value = it
            }

            Button(
                enabled = enable.value,
                onClick = {
                    enable.value = false
                    onUpdateClick(groupNameState.value, groupCommentState.value)
                }
            ) {
                Text("Update")
            }
        }
    }

}

@Composable
fun DeleteGroupConfirmDialog(
    theme: Theme,
    onDeleteConfirmOrCancelClick: (Boolean) -> Unit
) {
    Dialog(
        onCloseRequest = { onDeleteConfirmOrCancelClick(false) },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        val enable = remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.fillMaxSize().background(color = if (theme.isDark) Color.Black else Color.White),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswdDetailEditDialog(
    theme: Theme,
    onEditConfirmClick: (Passwd) -> Unit,
    onCloseClick: () -> Unit,
    passwd: Passwd,
) {
    val title = remember { mutableStateOf(passwd.title ?: "") }
    val username = remember { mutableStateOf(passwd.usernameString ?: "") }
    val password = remember { mutableStateOf(passwd.passwordString ?: "") }
    val link = remember { mutableStateOf(passwd.link ?: "") }
    val comment = remember { mutableStateOf(passwd.comment ?: "") }


    val textFieldEditEnable = remember { mutableStateOf(true) }
    Dialog(
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(600.dp, 600.dp)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(color = if (theme.isDark) Color.Black else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(horizontal = 100.dp),
            ) {
                TextField(
                    enabled = textFieldEditEnable.value,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    value = title.value,
                    maxLines = 1,
                    onValueChange = { newValue -> title.value = newValue },
                )
                TextField(
                    enabled = textFieldEditEnable.value,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    value = username.value,
                    maxLines = 1,
                    onValueChange = { username.value = it },
                )
                TextField(
                    enabled = textFieldEditEnable.value,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    value = password.value,
                    maxLines = 1,
                    onValueChange = { password.value = it },
                )
                TextField(
                    enabled = textFieldEditEnable.value,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("link", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    value = link.value,
                    maxLines = 1,
                    onValueChange = { link.value = it },
                )
                OutlinedTextField(
                    enabled = textFieldEditEnable.value,
                    modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Start),
                    label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    value = comment.value,
                    onValueChange = { comment.value = it },
                )

                val enable = remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        enabled = enable.value,
                        onClick = {
                            enable.value = false
                            textFieldEditEnable.value = false
                            onEditConfirmClick(
                                passwd.copy(
                                    title = title.value,
                                    usernameString = username.value,
                                    passwordString = password.value,
                                    link = link.value,
                                    comment = comment.value
                                )
                            )
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        enabled = enable.value,
                        onClick = {
                            enable.value = false
                            textFieldEditEnable.value = false
                            onCloseClick()
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}
