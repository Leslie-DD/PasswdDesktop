package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen

@Composable
fun App(
    viewModel: PasswdsViewModel,
    state: WindowState
) {
    Crossfade(
        targetState = viewModel.uiState.uiScreen,
        content = {
            MainScreen(viewModel, state, it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PasswdsViewModel,
    state: WindowState,
    uiScreen: UiScreen
) {
    val coroutine = rememberCoroutineScope()
    val scaffoldState = viewModel.scaffoldState
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        drawerContent = {
            when (uiScreen) {
                UiScreen.Settings, UiScreen.Passwds -> SideMenuScreen(
                    viewModel = viewModel,
                    modifier = Modifier.width(200.dp)
                )

                else -> {}
            }
        },
        drawerShape = customShape(
            customSize = Size(
                width = dpToPx(200.dp),
                height = dpToPx(state.size.height)
            )
        ),
        topBar = {
            TopAppBar(
                title = {
                    when (uiScreen) {
                        UiScreen.Settings, UiScreen.Passwds -> {

                            TopBarTitle(
                                uiScreen = uiScreen,
                                coroutine = coroutine,
                                scaffoldState = scaffoldState,
                                viewModel = viewModel
                            )
                        }

                        else -> {}
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            when (uiScreen) {
                UiScreen.Passwds -> PasswdsScreen(viewModel)
                UiScreen.Settings -> SettingsScreen(viewModel)
                UiScreen.Login -> LoginScreen(viewModel)
                UiScreen.Loading -> LoadingScreen()
            }
        }
    }
}

fun customShape(
    customSize: Size
) = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(Rect(0f, 0f, customSize.width, customSize.height))
    }
}

@Composable
fun dpToPx(dp: Dp) = with(LocalDensity.current) {
    dp.toPx()
}

