package ui.passwd

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entity.Passwd
import entity.defaultPasswd
import model.UiAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import ui.common.EditLimitTextField
import ui.common.copyToClipboard
import ui.common.defaultEditableTextFieldColors
import ui.common.defaultIconButtonColors

/**
 * 密码详情
 */
@Composable
fun PasswdDetailScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    var passwd by remember { mutableStateOf(defaultPasswd()) }
    Box(modifier.fillMaxSize()) {
        val passwdUiState = viewModel.passwdUiState.collectAsState().value
        passwdUiState.selectPasswd?.let {
            passwd = it

            var title by remember { mutableStateOf(passwd.title ?: "") }
            var username by remember { mutableStateOf(passwd.usernameString ?: "") }
            var password by remember { mutableStateOf(passwd.passwordString ?: "") }
            var link by remember { mutableStateOf(passwd.link ?: "") }
            var comment by remember { mutableStateOf(passwd.comment ?: "") }

            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PasswdDetails(
                        enabled = passwdUiState.editEnabled,
                        passwd = passwd,
                        title = title,
                        username = username,
                        password = password,
                        link = link,
                        comment = comment,
                        onTitleChange = { newValue -> title = newValue },
                        onUsernameChange = { newValue -> username = newValue },
                        onPasswordChange = { newValue -> password = newValue },
                        onLinkChange = { newValue -> link = newValue },
                        onCommentChange = { newValue -> comment = newValue }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        enabled = !passwdUiState.editEnabled && passwdUiState.editIconButtonEnabled,
                        colors = defaultIconButtonColors(),
                        onClick = {
                            viewModel.onAction(UiAction.UpdateEditEnabled(true))
                            title = passwd.title ?: ""
                            username = passwd.usernameString ?: ""
                            password = passwd.passwordString ?: ""
                            link = passwd.link ?: ""
                            comment = passwd.comment ?: ""
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    }

                    Row(modifier = Modifier.padding(start = 20.dp)) {
                        IconButton(
                            enabled = passwdUiState.editEnabled,
                            colors = defaultIconButtonColors(),
                            onClick = {
                                viewModel.onAction(UiAction.UpdateEditEnabled(false))
                                viewModel.onAction(UiAction.UpdateEditIconButtonEnabled(false))
                                viewModel.onAction(
                                    UiAction.UpdatePasswd(
                                        passwd.copy(
                                            title = title,
                                            usernameString = username,
                                            passwordString = password,
                                            link = link,
                                            comment = comment
                                        )
                                    )
                                )
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        }

                        IconButton(
                            enabled = passwdUiState.editEnabled,
                            colors = defaultIconButtonColors(),
                            onClick = {
                                viewModel.onAction(UiAction.UpdateEditEnabled(false))

                                title = passwd.title ?: ""
                                username = passwd.usernameString ?: ""
                                password = passwd.passwordString ?: ""
                                link = passwd.link ?: ""
                                comment = passwd.comment ?: ""
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Backspace, contentDescription = null)
                        }
                    }

                }
            }


            val dialogUiState = viewModel.dialogUiState.collectAsState().value
            when (dialogUiState.effect) {
                is DialogUiEffect.UpdatePasswdResult -> {
                    viewModel.onAction(UiAction.UpdateEditEnabled(false))
                    viewModel.onAction(UiAction.UpdateEditIconButtonEnabled(true))
                    viewModel.onAction(UiAction.ClearEffect)
                }

                else -> {}
            }
        }
    }
}

@Composable
fun PasswdDetails(
    enabled: Boolean,
    passwd: Passwd,
    title: String,
    username: String,
    password: String,
    link: String,
    comment: String,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EditLimitTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = "title",
            leadingIcon = Icons.Default.Title,
            value = if (enabled) title else passwd.title ?: "",
            onValueChange = { onTitleChange(it) },
        )
        EditLimitTextField(
            enabled = enabled,
            label = "username",
            leadingIcon = Icons.Default.People,
            value = if (enabled) username else passwd.usernameString ?: "",
            onValueChange = { onUsernameChange(it) },
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(
                        colors = defaultIconButtonColors(),
                        onClick = { (passwd.usernameString ?: "").copyToClipboard() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy username to clipboard"
                        )
                    }
                }
            }
        )
        var passwordVisible by remember { mutableStateOf(false) }
        EditLimitTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = "password",
            leadingIcon = Icons.Default.Lock,
            value = if (enabled) password else passwd.passwordString ?: "",
            onValueChange = { onPasswordChange(it) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(
                        colors = defaultIconButtonColors(),
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }

                    IconButton(
                        colors = defaultIconButtonColors(),
                        onClick = { (passwd.passwordString ?: "").copyToClipboard() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy password to clipboard"
                        )
                    }
                }
            }
        )

        EditLimitTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = "link",
            leadingIcon = Icons.Default.Link,
            value = if (enabled) link else passwd.link ?: "",
            onValueChange = { onLinkChange(it) },
        )
        OutlinedTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxSize().align(Alignment.Start),
            label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            value = if (enabled) comment else passwd.comment ?: "",
            onValueChange = { onCommentChange(it) },
            colors = defaultEditableTextFieldColors()
        )
    }
}

