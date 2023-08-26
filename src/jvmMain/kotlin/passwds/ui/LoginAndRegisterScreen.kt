package passwds.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.Res
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiEffect
import passwds.model.UiScreen

@Composable
fun LoginAndRegisterScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            IntroductionBox()
        }
        Box(
            modifier = Modifier.weight(0.6f).fillMaxHeight()
        ) {
            LoginAndRegisterBox(viewModel)
        }
    }
}

@Composable
private fun IntroductionBox() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppSymbolBox(modifier = Modifier.wrapContentHeight())
        Text(
            modifier = Modifier.padding(16.dp),
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            text = "Glad you are here! Passwd is a password management which encrypt by AES algorithm both in client and " +
                    "server. It means it's safe even there's no HTTPS since the data transferred between client and server is " +
                    "ciphertext and the server will always don't know your plaintext. But DO REMEMBER that WRITE your SECRET KEY " +
                    "on the notebook cause if you forget that, passwords you stored will decrypted incorrectly!"
        )
    }
}

@Composable
fun AppSymbolBox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
            contentDescription = null,
            modifier = Modifier.size(45.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Passwd",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Compose for Multiplatform", fontSize = 12.sp)
    }
}

@Composable
private fun LoginAndRegisterBox(viewModel: PasswdsViewModel) {
    val username = remember { mutableStateOf("lucas") }
    val password = remember { mutableStateOf("lucas_password") }
    val secretKey = remember { mutableStateOf("SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ=") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val isLogin = remember { mutableStateOf(true) }
        LazyRow(modifier = Modifier.wrapContentSize()) {
            screensListMenu(
                screens = UiScreen.LoginAndRegister,
                currentScreen = if (isLogin.value) UiScreen.Login else UiScreen.Register
            ) {
                isLogin.value = it is UiScreen.Login
            }
        }

        InfoBox(
            currentScreen = if (isLogin.value) UiScreen.Login else UiScreen.Register,
            username = username,
            password = password,
            secretKey = secretKey
        ) {
            viewModel.onAction(
                if (isLogin.value) {
                    UiAction.Login(
                        username = username.value,
                        password = password.value,
                        secretKey = secretKey.value
                    )
                } else {
                    UiAction.Register(
                        username = username.value,
                        password = password.value,
                    )
                }
            )

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
    val effect = viewModel.uiStateComposable.effect
    with(effect) {
        when (this) {
            is UiEffect.LoginAndRegisterFailure -> {
                msg.value = tipsMsg
                isTipsMsgDialogOpen.value = true
            }

            else -> {}
        }
    }
}

@Composable
private fun InfoBox(
    currentScreen: UiScreen,
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
        OutlinedEditTextBox(
            value = username.value,
            labelValue = "Username",
            imageVector = Icons.Outlined.People
        ) {
            username.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedEditTextBox(
            value = password.value,
            labelValue = "Password",
            imageVector = Icons.Outlined.Lock
        ) {
            password.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (currentScreen is UiScreen.Login) {
            OutlinedEditTextBox(
                value = secretKey.value,
                labelValue = "SecretKey",
                imageVector = Icons.Outlined.Key
            ) {
                secretKey.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            onClick = { onSubmitClick() }
        ) {
            Text(
                text = currentScreen.name
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedEditTextBox(
    enabled: Boolean = true,
    modifier: Modifier = Modifier.width(300.dp),
    value: String,
    labelValue: String,
    imageVector: ImageVector? = null,
    onInputChanged: (String) -> Unit
) {
    val text = remember { mutableStateOf(value) }
    OutlinedTextField(
        modifier = modifier,
        enabled = enabled,
        label = { Text(labelValue, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            imageVector?.let {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        },
        value = text.value,
        maxLines = 1,
        singleLine = true,
        onValueChange = {
            text.value = it
            onInputChanged(it)
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

fun LazyListScope.screensListMenu(
    screens: List<UiScreen>,
    currentScreen: UiScreen,
    onChoice: (screen: UiScreen) -> Unit
) {
    items(screens) { screen ->
        val isSelected = screen == currentScreen
        TextButton(
            interactionSource = remember { NoRippleInteractionSource() },
            onClick = { onChoice(screen) },
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        ) {
            Icon(imageVector = screen.icon, contentDescription = null)
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = screen.name, fontSize = 18.sp)
        }
    }
}