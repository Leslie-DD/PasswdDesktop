package ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import model.UiAction
import model.UiScreen
import model.uieffect.DialogUiEffect
import model.viewmodel.ConfigViewModel
import model.viewmodel.PasswdsViewModel
import ui.common.TipsDialog
import ui.login.LoginAndSignupScreen

@Composable
fun App(
    passwdsViewModel: PasswdsViewModel,
    configViewModel: ConfigViewModel
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
                    UiScreen.LoggedInScreen -> LoggedInScreen(windowUiState.uiScreen, passwdsViewModel, configViewModel, modifier)
                    UiScreen.LoginAndSignup -> LoginAndSignupScreen(passwdsViewModel, modifier)
                    UiScreen.Loadings -> LoadingScreen(modifier)

                    else -> {}
                }

                var isTipsDialogOpen by remember { mutableStateOf(false) }
                var tip by remember { mutableStateOf<String?>(null) }
                if (isTipsDialogOpen) {
                    TipsDialog(
                        warn = tip
                    ) {
                        isTipsDialogOpen = false
                        passwdsViewModel.onAction(UiAction.ClearEffect)
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
        )
    }
}
