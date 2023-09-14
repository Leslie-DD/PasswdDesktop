package passwds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import database.DataBase
import passwds.model.PasswdsViewModel

@Composable
fun SettingsScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.wrapContentSize(), horizontalAlignment = Alignment.Start
        ) {
            UsernameView(DataBase.instance.globalUsername.value)
            Spacer(modifier = Modifier.height(10.dp))
            SecretKeyView(DataBase.instance.globalSecretKey.value)
        }
    }
}

@Composable
fun UsernameView(username: String) {
    DetailTextField(
        modifier = Modifier.width(300.dp),
        label = "Username",
        leadingIcon = Icons.Outlined.People,
        value = username
    )
}

@Composable
fun SecretKeyView(secretKey: String) {
    var secretKeyVisible by remember { mutableStateOf(false) }
    DetailTextField(
        modifier = Modifier.width(300.dp),
        label = "Secret Key",
        leadingIcon = Icons.Default.Key,
        value = secretKey,
        visualTransformation = if (secretKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = {
                        secretKeyVisible = !secretKeyVisible
                    }) {
                    Icon(
                        imageVector = if (secretKeyVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (secretKeyVisible) "Hide secret key" else "Show secret key"
                    )
                }

                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = {
                        secretKey.copyToClipboard()
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
    OutlinedTextField(
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
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledBorderColor = MaterialTheme.colorScheme.secondary,
            disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectionColors = TextSelectionColors(handleColor = Color.White, backgroundColor = Color.Blue)
        )
    )
}