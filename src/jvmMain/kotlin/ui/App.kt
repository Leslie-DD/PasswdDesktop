package ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import model.UiScreen
import model.action.PasswdAction
import model.uieffect.DialogUiEffect
import model.viewmodel.PasswdsViewModel
import model.viewmodel.UiConfigViewModel
import model.viewmodel.UserViewModel
import ui.common.TipsDialog
import ui.login.LoginAndSignupScreen

@Composable
fun App(
    userViewModel: UserViewModel,
    passwdsViewModel: PasswdsViewModel,
    uiConfigViewModel: UiConfigViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        val windowUiState = passwdsViewModel.windowUiState.collectAsState().value
        val modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = windowUiState.uiScreens,
            content = {
                when (it) {
                    UiScreen.LoggedInScreen -> LoggedInScreen(windowUiState.uiScreen, passwdsViewModel, uiConfigViewModel, modifier)
                    UiScreen.LoginAndSignup -> LoginAndSignupScreen(userViewModel, passwdsViewModel, modifier)
                    UiScreen.Loadings -> LoadingScreen(modifier)
                    else -> {}
                }
            }
        )

        var isTipsDialogOpen by remember { mutableStateOf(false) }
        var tip by remember { mutableStateOf<String?>(null) }
        if (isTipsDialogOpen) {
            TipsDialog(warn = tip) {
                isTipsDialogOpen = false
                passwdsViewModel.onAction(PasswdAction.ClearEffect)
            }
        }

        val dialogUiState = passwdsViewModel.dialogUiState.collectAsState().value
        with(dialogUiState.effect) {
            when (this) {
                is DialogUiEffect.LoginAndSignupFailure -> {
                    tip = tipsMsg
                    isTipsDialogOpen = true
                }

                else -> {}
            }
        }
    }
}
