package ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.action.LoginAction
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UserViewModel

@Composable
fun LoginInfoBox(
    userViewModel: UserViewModel,
    passwdsViewModel: PasswdsViewModel,
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit
) {
    val loginUiState = userViewModel.loginUiState.collectAsState().value

    var username by remember { mutableStateOf(loginUiState.userData.username) }
    var password by remember { mutableStateOf(if (loginUiState.userData.saved) loginUiState.userData.password else "") }
    var secretKey by remember { mutableStateOf(if (loginUiState.userData.saved) loginUiState.userData.secretKey else "") }
    var host by remember { mutableStateOf(if (loginUiState.userData.saved) loginUiState.userData.host else "") }
    var port by remember { mutableStateOf(if (loginUiState.userData.saved) loginUiState.userData.port else 8080) }
    var saved by remember { mutableStateOf(loginUiState.userData.saved) }
    var silentlyLogin by remember { mutableStateOf(loginUiState.userData.silentlyLogin) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val histories = userViewModel.loginUiState.collectAsState().value.userDataList
        UsernameTextField(
            enabled = enabled,
            enabledDropMenu = true,
            value = username,
            histories = histories,
            onHistorySelected = { item ->
                username = item.username
                password = item.password
                secretKey = item.secretKey
                host = item.host
                port = item.port
                saved = item.saved
                silentlyLogin = item.silentlyLogin
            },
            onUsernameChanged = { username = it },
        )
        PasswdTextField(
            enabled = enabled,
            value = password
        ) { password = it }
        SecretKeyTextField(
            enabled = enabled,
            value = secretKey
        ) { secretKey = it }
        HostTextField(
            enabled = enabled,
            hostValue = host,
            portValue = port.toString(),
            histories = histories,
            onHostChanged = {
                host = it
            },
            onPortChanged = {
                port = Integer.valueOf(it)
            },
            onHistorySelected = { item ->
                host = item.host
                port = item.port
            },
        )

        LoginSelections(
            enabled = enabled,
            save = saved,
            onSaveClick = { save ->
                saved = save
                if (!save && silentlyLogin) {
                    silentlyLogin = false
                }
            },
            silentlyLogin = silentlyLogin,
            onSilentlyLoginClick = { silentlyLoginValue ->
                silentlyLogin = silentlyLoginValue
                if (silentlyLoginValue) {
                    saved = true
                }
            },
        )

        Button(
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            enabled = enabled,
            onClick = {
                setEnabled(false)
                userViewModel.onAction(
                    LoginAction.Login(
                        username = username,
                        password = password,
                        secretKey = secretKey,
                        host = host,
                        port = port,
                        saved = saved,
                        silentlyLogin = silentlyLogin
                    )
                )
            }
        ) {
            Text(
                text = "Login"
            )
        }
    }
}

@Composable
private fun LoginSelections(
    enabled: Boolean,
    save: Boolean,
    onSaveClick: (Boolean) -> Unit,
    silentlyLogin: Boolean,
    onSilentlyLoginClick: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            enabled = enabled,
            selected = save,
            onClick = { onSaveClick(!save) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.tertiaryContainer,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
        Text(
            text = "Save",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.width(20.dp))

        RadioButton(
            enabled = enabled,
            selected = silentlyLogin,
            onClick = { onSilentlyLoginClick(!silentlyLogin) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.tertiaryContainer,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
        Text(
            text = "Silently login",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}