package passwds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import passwds.model.PasswdsViewModel
import passwds.model.UiAction

@Composable
fun LoginScreen(
    viewModel: PasswdsViewModel
) {

    val username = remember { mutableStateOf("lucas") }
    val password = remember { mutableStateOf("lucas_password") }
    val secretKey = remember { mutableStateOf("SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ=") }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppSymbolBox()
            Spacer(modifier = Modifier.height(30.dp))

            Username(viewModel = viewModel) {
                username.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))
            Password(viewModel = viewModel) {
                password.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))
            SecretKey(viewModel = viewModel) {
                secretKey.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    viewModel.onAction(
                        UiAction.Login(
                            username = username.value,
                            password = password.value,
                            secretKey = secretKey.value
                        )
                    )
                }
            ) {
                Text("登录")
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Username(
    viewModel: PasswdsViewModel,
    onInputChanged: (String) -> Unit
) {
    val text = remember { mutableStateOf("lucas") }
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text("Username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.People, contentDescription = null)
        },
        value = text.value,
        maxLines = 1,
        singleLine = true,
        onValueChange = {
            text.value = it
            onInputChanged(it)
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Password(
    viewModel: PasswdsViewModel,
    onInputChanged: (String) -> Unit
) {
    val text = remember { mutableStateOf("lucas_password") }
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text("Password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
        },
        value = text.value,
        maxLines = 1,
        singleLine = true,
        onValueChange = {
            text.value = it
            onInputChanged(it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretKey(
    viewModel: PasswdsViewModel,
    onInputChanged: (String) -> Unit
) {
    val text = remember { mutableStateOf("SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ=") }
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text("SecretKey", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Key, contentDescription = null)
        },
        value = text.value,
        maxLines = 1,
        singleLine = true,
        onValueChange = {
            text.value = it
            onInputChanged(it)
        },
    )
}