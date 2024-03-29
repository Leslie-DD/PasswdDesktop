package ui.passwd

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Details
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Close
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
import model.action.PasswdAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import ui.common.EditLimitTextField
import ui.common.copyToClipboard
import ui.common.defaultEditableTextFieldColors
import ui.common.defaultIconButtonColors
import java.awt.Desktop
import java.net.URI

/**
 * 密码详情
 */
@Composable
fun PasswdDetailScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    var passwd by remember { mutableStateOf(defaultPasswd()) }
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
            ) {
                IconButton(
                    enabled = true,
                    colors = defaultIconButtonColors(),
                    onClick = {
                        viewModel.onAction(PasswdAction.UpdateEditEnabled(editEnabled = !passwdUiState.editEnabled))
                        title = passwd.title ?: ""
                        username = passwd.usernameString ?: ""
                        password = passwd.passwordString ?: ""
                        link = passwd.link ?: ""
                        comment = passwd.comment ?: ""
                    }
                ) {
                    Icon(
                        imageVector = if (!passwdUiState.editEnabled) Icons.Default.Edit else Icons.Rounded.Close,
                        contentDescription = if (!passwdUiState.editEnabled) "Edit" else "Close"
                    )
                }

                IconButton(
                    enabled = passwdUiState.editEnabled,
                    colors = defaultIconButtonColors(),
                    onClick = {
                        viewModel.onAction(PasswdAction.UpdateEditEnabled(false))
                        viewModel.onAction(
                            PasswdAction.UpdatePasswd(
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
                    Icon(imageVector = Icons.Default.Check, contentDescription = "submit")
                }
            }
        }


        val dialogUiState = viewModel.dialogUiState.collectAsState().value
        when (dialogUiState.effect) {
            is DialogUiEffect.UpdatePasswdResult -> {
                viewModel.onAction(PasswdAction.UpdateEditEnabled(false))
                viewModel.onAction(PasswdAction.ClearEffect)
            }

            else -> {}
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
        val usernameStr = if (enabled) username else passwd.usernameString ?: ""
        EditLimitTextField(
            enabled = enabled,
            label = "username",
            leadingIcon = Icons.Default.People,
            value = usernameStr,
            onValueChange = { onUsernameChange(it) },
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize()) {
                    IconButton(
                        enabled = username.isNotBlank(),
                        colors = defaultIconButtonColors(),
                        onClick = { usernameStr.copyToClipboard() }
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
        val passwordStr = if (enabled) password else passwd.passwordString ?: ""
        EditLimitTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = "password",
            leadingIcon = Icons.Default.Lock,
            value = passwordStr,
            onValueChange = { onPasswordChange(it) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize()) {
                    IconButton(
                        enabled = passwordStr.isNotBlank(),
                        colors = defaultIconButtonColors(),
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }

                    IconButton(
                        enabled = passwordStr.isNotBlank(),
                        colors = defaultIconButtonColors(),
                        onClick = { passwordStr.copyToClipboard() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy password to clipboard"
                        )
                    }
                }
            }
        )

        val linkStr = if (enabled) link else passwd.link ?: ""
        EditLimitTextField(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = "link",
            leadingIcon = Icons.Default.Link,
            value = linkStr,
            onValueChange = { onLinkChange(it) },
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize()) {
                    IconButton(
                        enabled = linkStr.isNotBlank(),
                        colors = defaultIconButtonColors(),
                        onClick = {
                            try {
                                Desktop.getDesktop().browse(URI.create(linkStr))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Details,
                            contentDescription = "go to this site"
                        )
                    }

                    IconButton(
                        enabled = linkStr.isNotBlank(),
                        colors = defaultIconButtonColors(),
                        onClick = { linkStr.copyToClipboard() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy link to clipboard"
                        )
                    }
                }
            }
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

