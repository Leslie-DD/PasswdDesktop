package ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.action.PasswdAction
import model.viewmodel.PasswdsViewModel

@Composable
fun LoginInfoBox(
    viewModel: PasswdsViewModel,
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit
) {
    val loginUiState = viewModel.loginUiState.collectAsState().value

    var username by remember { mutableStateOf(loginUiState.historyData.username) }
    var password by remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.password else "") }
    var secretKey by remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.secretKey else "") }
    var host by remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.host else "") }
    var port by remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.port else 8080) }
    var saved by remember { mutableStateOf(loginUiState.historyData.saved) }
    var silentlyLogin by remember { mutableStateOf(loginUiState.historyData.silentlyLogin) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val histories = viewModel.loginUiState.collectAsState().value.historyDataList
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
                viewModel.onAction(PasswdAction.InitHost(Pair(host, port)))
            },
            onPortChanged = {
                port = Integer.valueOf(it)
                viewModel.onAction(PasswdAction.InitHost(Pair(host, port)))
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
                viewModel.onAction(
                    PasswdAction.Login(
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