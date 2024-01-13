package ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import model.action.LoginAction
import model.action.PasswdAction
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UserViewModel

@Composable
fun SignupInfoBox(
    userViewModel: UserViewModel,
    passwdsViewModel: PasswdsViewModel,
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(8080) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val histories = userViewModel.loginUiState.collectAsState().value.historyDataList
        UsernameTextField(
            enabled = enabled,
            value = username
        ) { username = it }
        PasswdTextField(
            enabled = enabled,
            value = password
        ) { password = it }
        HostTextField(
            enabled = enabled,
            hostValue = host,
            portValue = port.toString(),
            histories = histories,
            onHostChanged = {
                host = it
                passwdsViewModel.onAction(PasswdAction.InitHost(Pair(host, port)))
            },
            onPortChanged = {
                port = Integer.valueOf(it)
                passwdsViewModel.onAction(PasswdAction.InitHost(Pair(host, port)))
            },
            onHistorySelected = {
                host = it.host
                port = it.port
            }
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            enabled = enabled,
            onClick = {
                setEnabled(false)
                userViewModel.onAction(LoginAction.Signup(username, password, host, port))
            }
        ) {
            Text(
                text = "Sign up"
            )
        }
    }
}