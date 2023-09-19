package ui.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import database.entity.HistoryData
import model.PasswdsViewModel
import model.UiAction
import model.UiScreen
import model.uieffect.DialogUiEffect
import ui.EnabledOutlinedTextField
import ui.NoRippleInteractionSource
import ui.TipsDialog

@Composable
fun LoginAndSignupScreen(
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
            LoginAndSignupBox(viewModel)
        }
    }
}

@Composable
private fun LoginAndSignupBox(viewModel: PasswdsViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        var currentScreen by remember { mutableStateOf<UiScreen>(UiScreen.Login) }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(modifier = Modifier.wrapContentSize()) {
            screensListMenu(
                screens = UiScreen.LoginAndSignup,
                currentScreen = currentScreen
            ) { currentScreen = it }
        }
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = currentScreen,
            content = {
                if (it is UiScreen.Login) {
                    LoginInfoBox(viewModel = viewModel)
                } else if (it is UiScreen.Signup) {
                    SignupInfoBox(viewModel = viewModel)
                }
            }
        )
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
            is DialogUiEffect.SignupResult -> {
                tipsDialogTitle.value = "Successfully signed up!"
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
fun UsernameTextField(
    enabledDropMenu: Boolean = false,
    value: String,
    histories: List<HistoryData> = arrayListOf(),
    onHistorySelected: (HistoryData) -> Unit = {},
    onUsernameChanged: (String) -> Unit
) {
    var menuVisible by remember { mutableStateOf(false) }
    EnabledOutlinedTextField(
        value = value,
        labelValue = "Username",
        leadingIconImageVector = Icons.Outlined.People,
        trailingIcon = if (enabledDropMenu) {
            {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 10.dp)
                        .focusProperties { canFocus = false }
                ) {
                    HistoriesDropDownMenu(
                        histories = histories,
                        expanded = menuVisible,
                        offset = DpOffset((-200).dp, 0.dp),
                        onHistorySelected = onHistorySelected
                    ) { menuVisible = it }

                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = { menuVisible = !menuVisible }
                    ) {
                        Icon(
                            imageVector = if (menuVisible) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = if (menuVisible) "collapse menu list" else "expand menu list"
                        )
                    }
                }
            }
        } else null,
        onFocusChanged = { hasFocus ->
            if (hasFocus && value.isEmpty()) {
                menuVisible = true
            }
        },
    ) {
        onUsernameChanged(it)
        if (it.isEmpty()) {
            menuVisible = true
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun PasswdTextField(
    value: String,
    onPasswordChanged: (String) -> Unit
) {
    EnabledOutlinedTextField(
        value = value,
        labelValue = "Password",
        leadingIconImageVector = Icons.Outlined.Lock,
        disableContentEncrypt = false
    ) {
        onPasswordChanged(it)
    }

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun SecretKeyTextField(
    value: String,
    onSecretKeyChanged: (String) -> Unit
) {
    EnabledOutlinedTextField(
        value = value,
        labelValue = "SecretKey",
        leadingIconImageVector = Icons.Outlined.Key,
        disableContentEncrypt = false
    ) {
        onSecretKeyChanged(it)
    }

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun HistoriesDropDownMenu(
    histories: List<HistoryData>,
    expanded: Boolean,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onHistorySelected: (HistoryData) -> Unit,
    onMenuVisibleChanged: (Boolean) -> Unit
) {
    var selectedMenuIndex by remember { mutableStateOf(-1) }
    DropdownMenu(
        modifier = Modifier.padding(10.dp).width(200.dp).background(color = MaterialTheme.colorScheme.surface),
        offset = offset,
        expanded = expanded,
        onDismissRequest = { onMenuVisibleChanged(false) },
    ) {
        histories.forEachIndexed { index, item ->
            HistoryMenuItem(
                item = item,
                selected = selectedMenuIndex == index,
                onItemSelected = {
                    onHistorySelected(item)
                    selectedMenuIndex = index
                    onMenuVisibleChanged(false)
                }
            )
        }
    }
}

@Composable
private fun HistoryMenuItem(
    item: HistoryData,
    selected: Boolean,
    onItemSelected: () -> Unit,
) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20),
        interactionSource = remember { NoRippleInteractionSource() },
        onClick = { onItemSelected() },
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (selected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    ) {
        Text(
            text = item.username,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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