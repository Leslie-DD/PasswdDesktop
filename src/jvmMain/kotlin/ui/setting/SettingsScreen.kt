package ui.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import database.DataBase
import model.viewmodel.UiConfigViewModel
import model.viewmodel.PasswdsViewModel
import ui.common.EditLimitTextField
import ui.common.copyToClipboard
import ui.common.defaultIconButtonColors
import ui.toolbar.SideMenuBar

@Composable
fun SettingsScreen(
    passwdsViewModel: PasswdsViewModel,
    uiConfigViewModel: UiConfigViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SideMenuBar(passwdsViewModel, uiConfigViewModel)
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
}

@Composable
fun UsernameView(username: String) {
    EditLimitTextField(
        modifier = Modifier.width(300.dp),
        label = "Username",
        leadingIcon = Icons.Outlined.People,
        value = username
    )
}

@Composable
fun SecretKeyView(secretKey: String) {
    var secretKeyVisible by remember { mutableStateOf(false) }
    EditLimitTextField(
        modifier = Modifier.width(300.dp),
        label = "Secret Key",
        leadingIcon = Icons.Default.Key,
        value = secretKey,
        visualTransformation = if (secretKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Row(modifier = Modifier.wrapContentSize().padding(end = 10.dp)) {
                IconButton(
                    colors = defaultIconButtonColors(),
                    onClick = {
                        secretKeyVisible = !secretKeyVisible
                    }) {
                    Icon(
                        imageVector = if (secretKeyVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (secretKeyVisible) "Hide secret key" else "Show secret key"
                    )
                }

                IconButton(
                    colors = defaultIconButtonColors(),
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
