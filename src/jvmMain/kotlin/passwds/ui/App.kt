package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        val uiScreen = viewModel.uiStateComposable.uiScreen
        when (uiScreen) {
            UiScreen.Passwds, UiScreen.Settings -> {
                SideMenuScreen(viewModel)
            }

            else -> {}
        }

        val modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = uiScreen,
            content = {

                viewModel.logger.info("Crossfade ${it.name}")
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
