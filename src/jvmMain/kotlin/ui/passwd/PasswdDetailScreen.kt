package ui.passwd

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entity.Passwd
import model.UiAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import ui.common.EditPasswdDialog
import ui.common.ReadableTextField
import ui.common.copyToClipboard

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
                EditPasswdDialog(
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
fun PasswdDetails(
    passwd: Passwd
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ReadableTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "title",
            leadingIcon = Icons.Default.Title,
            value = passwd.title ?: ""
        )
        ReadableTextField(
            label = "username",
            leadingIcon = Icons.Default.People,
            value = passwd.usernameString ?: "",
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            (passwd.usernameString ?: "").copyToClipboard()
                        }
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
        ReadableTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "password",
            leadingIcon = Icons.Default.Lock,
            value = passwd.passwordString ?: "",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }

                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            (passwd.passwordString ?: "").copyToClipboard()
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

        ReadableTextField(
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