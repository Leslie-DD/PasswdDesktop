package passwds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import passwds.model.PasswdsViewModel

@Composable
fun SettingsScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.wrapContentSize(), horizontalAlignment = Alignment.Start
        ) {

            UsernameView(viewModel.settings.username.value)
            Spacer(modifier = Modifier.height(10.dp))
            PasswordView("")
            Spacer(modifier = Modifier.height(10.dp))
            SecretKeyView(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameView(username: String) {
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = false,
        label = { Text("Username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.People, contentDescription = null)
        },
        value = username,
        maxLines = 1,
        singleLine = true,
        onValueChange = {},
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordView(password: String) {
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = false,
        label = { Text("Password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
        },
        value = password,
        maxLines = 1,
        singleLine = true,
        onValueChange = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretKeyView(viewModel: PasswdsViewModel) {
    var secretKeyVisible by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.wrapContentWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        val secretKey by viewModel.settings.secretKey.collectAsState()
        var text by remember {
            mutableStateOf("")
        }
        var enableEdit by remember { mutableStateOf(false) }
        OutlinedTextField(modifier = Modifier.width(300.dp),
            enabled = enableEdit,
            label = {
                Text(
                    text = "Secret Key",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Key, contentDescription = null)
            },
            value = if (enableEdit) text else secretKey,
            maxLines = 1,
            singleLine = true,
            onValueChange = {
                text = it
            },
            visualTransformation = if (secretKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Box(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                    IconButton(onClick = { secretKeyVisible = !secretKeyVisible }) {
                        Icon(
                            imageVector = if (secretKeyVisible) {
                                Icons.Filled.Visibility

                            } else {
                                Icons.Filled.VisibilityOff
                            }, contentDescription = if (secretKeyVisible) "Hide password" else "Show password"
                        )
                    }
                }
            })
        IconButton(onClick = {
            enableEdit = !enableEdit
        }) {
            Icon(
                imageVector = if (enableEdit) Icons.Outlined.Cancel else Icons.Outlined.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(enabled = enableEdit, onClick = {
            viewModel.settings.secretKey.tryEmit(text)
            enableEdit = false
        }) {
            Icon(
                imageVector = Icons.Outlined.ArrowCircleUp, contentDescription = null, tint = if (enableEdit) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}