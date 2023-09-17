package passwds.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiScreen

@Composable
fun SideMenuScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    val windowUiState = viewModel.windowUiState.collectAsState().value
    val expand = windowUiState.menuOpen
    Column(
        modifier = modifier
            .width(if (expand) 160.dp else 60.dp)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surface
            )
            .padding(top = 30.dp, bottom = 20.dp, start = 6.dp, end = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expand) {
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = "Passwd",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = { viewModel.onAction(UiAction.MenuOpenOrClose(!expand)) }
                    ) {
                        Icon(
                            imageVector = if (expand) Icons.Default.MenuOpen else Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            }

            items(UiScreen.Screens) { screen ->
                val isSelected = screen == viewModel.windowUiState.collectAsState().value.uiScreen
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20),
                    interactionSource = remember { NoRippleInteractionSource() },
                    onClick = {
                        viewModel.onAction(UiAction.GoScreen(screen))
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(imageVector = screen.icon, contentDescription = null)
                    if (expand) {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(text = screen.name, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeChoiceButton(viewModel, expand)
            Spacer(modifier = modifier.height(15.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = Color.White
                ),
                onClick = {
                    viewModel.onAction(UiAction.GoScreen(UiScreen.Login))
                }
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                if (expand) {
                    Spacer(modifier = Modifier.width(10.dp).height(40.dp))
                    Text(text = "退出登录")
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemeChoiceButton(viewModel: PasswdsViewModel, expand: Boolean) {
    val theme by viewModel.theme.collectAsState()

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        interactionSource = remember { NoRippleInteractionSource() },
        onClick = {
            viewModel.updateTheme()
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = theme,
            transitionSpec = {
                fadeIn() + slideInHorizontally() with fadeOut() + slideOutVertically()
            }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.animateContentSize()
            ) {
                Icon(
                    imageVector = theme.iconVector,
                    contentDescription = null
                )
                if (expand) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = theme.name)
                }
            }
        }
    }

}

/**
 * 无点击效果 InteractionSource
 */
class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}
