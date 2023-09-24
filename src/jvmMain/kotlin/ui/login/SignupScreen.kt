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
import model.UiAction
import model.viewmodel.PasswdsViewModel

@Composable
fun SignupInfoBox(
    viewModel: PasswdsViewModel
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
        val histories = viewModel.loginUiState.collectAsState().value.historyDataList
        UsernameTextField(value = username) { username = it }
        PasswdTextField(value = password) { password = it }
        HostTextField(
            hostValue = host,
            portValue = port.toString(),
            histories = histories,
            onHostChanged = {
                host = it
                viewModel.onAction(UiAction.InitHost(Pair(host, port)))
            },
            onPortChanged = {
                port = Integer.valueOf(it)
                viewModel.onAction(UiAction.InitHost(Pair(host, port)))
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
            onClick = { viewModel.onAction(UiAction.Signup(username, password, host, port)) }
        ) {
            Text(
                text = "Sign up"
            )
        }
    }
}