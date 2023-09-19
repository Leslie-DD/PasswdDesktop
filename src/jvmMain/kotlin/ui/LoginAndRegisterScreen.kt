package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.PasswdsViewModel
import model.Res
import model.UiAction
import model.UiScreen
import model.uieffect.DialogUiEffect

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
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(0f, 20f, 20f, 0f)
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
                    "ciphertext and the server will always don't know your plaintext. But DO REMEMBER that WRITE DOWN your " +
                    "SECRET KEY cause if you forget that, passwords you stored will decrypted incorrectly!"
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
    val loginUiState = viewModel.loginUiState.collectAsState().value

    val username = remember { mutableStateOf(loginUiState.historyData.username) }
    val password = remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.password else "") }
    val secretKey = remember { mutableStateOf(if (loginUiState.historyData.saved) loginUiState.historyData.secretKey else "") }
    val saved = remember { mutableStateOf(loginUiState.historyData.saved) }
    val silentlySignIn = remember { mutableStateOf(loginUiState.historyData.silentlySignIn) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val isLogin = remember { mutableStateOf(true) }
        Spacer(modifier = Modifier.height(20.dp))
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
            secretKey = secretKey,
            saved = saved,
            onSaveClick = { save ->
                saved.value = save
                if (!save && silentlySignIn.value) {
                    silentlySignIn.value = false
                }
            },
            silentlySignIn = silentlySignIn,
            onSilentlySignInClick = { silentlySignInValue ->
                silentlySignIn.value = silentlySignInValue
                if (silentlySignInValue) {
                    saved.value = true
                }
            }
        ) {
            viewModel.onAction(
                if (isLogin.value) {
                    UiAction.Login(
                        username = username.value,
                        password = password.value,
                        secretKey = secretKey.value,
                        saved = saved.value,
                        silentlySignIn = silentlySignIn.value
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

    val isImportantTipsDialogOpen = remember { mutableStateOf(false) }
    val tipsDialogTitle = remember { mutableStateOf("") }
    val tipsDialogInfo = remember { mutableStateOf<String?>(null) }
    val tipsDialogWarn = remember { mutableStateOf<String?>(null) }
    if (isImportantTipsDialogOpen.value) {
        TipsDialog(
            title = tipsDialogTitle.value,
            infoLabel = "Secret Key",
            info = tipsDialogInfo.value,
            warn = tipsDialogWarn.value,
            buttonValue = "Got it, I've written it down!"
        ) {
            isImportantTipsDialogOpen.value = false
            viewModel.onAction(UiAction.ClearEffect)
            viewModel.onAction(UiAction.GoScreen(UiScreen.Passwds))
        }
    }
    val dialogUiState = viewModel.dialogUiState.collectAsState().value
    with(dialogUiState.effect) {
        when (this) {
            is DialogUiEffect.RegisterResult -> {
                tipsDialogTitle.value = "Successfully Registered!"
                tipsDialogInfo.value = this.secretKey
                tipsDialogWarn.value =
                    "Please make sure WRITE DOWN your secret key, or there will be severe problem while encrypting/decrypting passwords."
                isImportantTipsDialogOpen.value = true
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
    saved: MutableState<Boolean>,
    onSaveClick: (Boolean) -> Unit,
    silentlySignIn: MutableState<Boolean>,
    onSilentlySignInClick: (Boolean) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnabledOutlinedTextField(
            value = username.value,
            labelValue = "Username",
            leadingIconImageVector = Icons.Outlined.People
        ) {
            username.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        EnabledOutlinedTextField(
            value = password.value,
            labelValue = "Password",
            leadingIconImageVector = Icons.Outlined.Lock,
            disableContentEncrypt = false
        ) {
            password.value = it
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (currentScreen is UiScreen.Login) {
            EnabledOutlinedTextField(
                value = secretKey.value,
                labelValue = "SecretKey",
                leadingIconImageVector = Icons.Outlined.Key,
                disableContentEncrypt = false
            ) {
                secretKey.value = it
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = saved.value,
                    onClick = { onSaveClick(!saved.value) },
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
                    selected = silentlySignIn.value,
                    onClick = { onSilentlySignInClick(!silentlySignIn.value) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.tertiaryContainer,
                        unselectedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "Silently sign in",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Button(
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            onClick = { onSubmitClick() }
        ) {
            Text(
                text = currentScreen.name
            )
        }
    }
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
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (isSelected) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        ) {
            Icon(imageVector = screen.icon, contentDescription = null)
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = screen.name, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(15.dp))
    }
}