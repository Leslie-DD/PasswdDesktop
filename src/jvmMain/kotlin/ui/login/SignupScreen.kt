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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UsernameTextField(value = username) { username = it }
        PasswdTextField(value = password) { password = it }
        Button(
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            onClick = {
                viewModel.onAction(
                    UiAction.Signup(
                        username = username,
                        password = password,
                    )
                )
            }
        ) {
            Text(
                text = "Sign up"
            )
        }
    }
}