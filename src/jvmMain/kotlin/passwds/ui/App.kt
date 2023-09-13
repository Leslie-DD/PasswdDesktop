package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen

@Composable
fun App(
    viewModel: PasswdsViewModel,
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        val windowUiState = viewModel.windowUiState.collectAsState().value
        when (windowUiState.uiScreen) {
            UiScreen.Passwds, UiScreen.Settings -> {
                SideMenuScreen(viewModel)
            }

            else -> {}
        }

        val modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = windowUiState.uiScreen,
            content = {
                when (it) {
                    UiScreen.Passwds -> PasswdsScreen(viewModel, modifier)
                    UiScreen.Settings -> SettingsScreen(viewModel, modifier)
                    UiScreen.Loading -> LoadingScreen(modifier)
                    in UiScreen.LoginAndRegister -> LoginAndRegisterScreen(viewModel, modifier)

                    else -> {}
                }
            }
        )
    }
}
