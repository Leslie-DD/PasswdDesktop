package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen

@Composable
fun App(
    viewModel: PasswdsViewModel,
) {
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = viewModel.uiState.uiScreen,
        content = {
            MainScreen(viewModel, it)
        }
    )
}

@Composable
fun MainScreen(
    viewModel: PasswdsViewModel,
    uiScreen: UiScreen
) {
    Box(
        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            when (uiScreen) {
                UiScreen.Passwds -> {
                    SideMenuScreen(viewModel)
                    PasswdsScreen(viewModel)
                }

                UiScreen.Settings -> {
                    SideMenuScreen(viewModel)
                    SettingsScreen(viewModel)
                }

                UiScreen.Loading -> LoadingScreen()

                in UiScreen.LoginAndRegister -> LoginAndRegisterScreen(viewModel)

                else -> {}
            }
        }
    }
}

