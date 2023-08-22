package passwds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiEffect
import passwds.model.UiScreen

@Composable
fun LoginAndRegisterScreen(
    viewModel: PasswdsViewModel
) {
    val username = remember { mutableStateOf("lucas") }
    val password = remember { mutableStateOf("lucas_password") }
    val secretKey = remember { mutableStateOf("SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ=") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val uiScreen = viewModel.uiState.uiScreen
        LazyRow(modifier = Modifier.wrapContentSize()) {
            screensListMenu(UiScreen.LoginAndRegister, uiScreen) {
                viewModel.onAction(UiAction.GoScreen(it))
            }
        }

        InfoBox(uiScreen, username, password, secretKey) {
            when (uiScreen) {
                is UiScreen.Login -> {
                    viewModel.onAction(
                        UiAction.Login(
                            username = username.value,
                            password = password.value,
                            secretKey = secretKey.value
                        )
                    )
                }

                is UiScreen.Register -> {
                    viewModel.onAction(
                        UiAction.Register(
                            username = username.value,
                            password = password.value,
                        )
                    )
                }

                else -> {}
            }
        }
    }

    val isTipsMsgDialogOpen = remember { mutableStateOf(false) }
    val msg = remember { mutableStateOf<String?>(null) }
    if (isTipsMsgDialogOpen.value) {
        val theme by viewModel.theme.collectAsState()
        TipsMessage(
            msg = msg.value,
            theme = theme,
        ) {
            isTipsMsgDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)
        }
    }
    val effect = viewModel.uiState.effect
    with(effect) {
        when (this) {
            is UiEffect.LoginAndRegisterFailure -> {
//                Log.error("receive LoginAndRegisterFailure Effect, tipsMsg = $tipsMsg")
                msg.value = tipsMsg
                isTipsMsgDialogOpen.value = true
            }

            else -> {}
        }
    }
}

@Composable
private fun InfoBox(
    uiScreen: UiScreen,
    username: MutableState<String>,
    password: MutableState<String>,
    secretKey: MutableState<String>,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppSymbolBox()
        Spacer(modifier = Modifier.height(30.dp))

        EditTextBox(
            value = username.value,
            labelValue = "Username",
            imageVector = Icons.Outlined.People
        ) {
            username.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        EditTextBox(
            value = password.value,
            labelValue = "Password",
            imageVector = Icons.Outlined.Lock
        ) {
            password.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (uiScreen is UiScreen.Login) {
            EditTextBox(
                value = secretKey.value,
                labelValue = "SecretKey",
                imageVector = Icons.Outlined.Key
            ) {
                secretKey.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = { onSubmitClick() }
        ) {
            Text(
                text = uiScreen.name
            )
        }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextBox(
    value: String,
    labelValue: String,
    imageVector: ImageVector,
    onInputChanged: (String) -> Unit
) {
    val text = remember { mutableStateOf(value) }
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text(labelValue, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = imageVector, contentDescription = null)
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