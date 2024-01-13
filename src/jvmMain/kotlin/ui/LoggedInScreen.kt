package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import model.UiScreen
import model.viewmodel.UiConfigViewModel
import model.viewmodel.PasswdsViewModel
import ui.passwd.PasswdsScreen
import ui.setting.SettingsScreen

@Composable
fun LoggedInScreen(
    uiScreen: UiScreen,
    passwdsViewModel: PasswdsViewModel,
    uiConfigViewModel: UiConfigViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (uiScreen) {
            UiScreen.Passwds -> PasswdsScreen(
                passwdsViewModel = passwdsViewModel,
                uiConfigViewModel = uiConfigViewModel,
                modifier = modifier
            )

            UiScreen.Settings -> SettingsScreen(passwdsViewModel, uiConfigViewModel, modifier)
            else -> {}
        }
    }
}