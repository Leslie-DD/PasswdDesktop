package passwds.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import model.Res
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SideMenuScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {

    val coroutine = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxSize()) {
        val uiScreen = viewModel.uiState.uiScreen
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                AppSymbolBox(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                Color.Transparent,
                            )
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            screensListMenu(viewModel, uiScreen)

            item { Spacer(modifier = Modifier.height(80.dp)) }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = {
                    viewModel.onAction(UiAction.GoScreen(UiScreen.Login))
                    coroutine.launch {
                        viewModel.scaffoldState.drawerState.close()
                    }
                },
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    ), alpha = 0.4f, shape = RoundedCornerShape(8.dp)
                )
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp).height(40.dp))
                Text(text = "退出登录")
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
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

fun LazyListScope.screensListMenu(
    viewModel: PasswdsViewModel,
    currentScreen: UiScreen,
) {
    items(UiScreen.Screens) { screen ->
        val isSelected = screen == currentScreen
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = 10.dp, top = 5.dp, bottom = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            val coroutine = rememberCoroutineScope()
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = isSelected,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(bottomEndPercent = 50, topEndPercent = 50)
                        )
                    )
                }
            }

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                interactionSource = remember { NoRippleInteractionSource() },
                onClick = {
                    viewModel.onAction(UiAction.GoScreen(screen))
                    coroutine.launch {
                        viewModel.scaffoldState.drawerState.close()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Spacer(modifier = Modifier.weight(0.4f))
                Icon(imageVector = screen.icon, contentDescription = null)
                Spacer(modifier = Modifier.width(15.dp).fillMaxHeight())
                Text(text = screen.name, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(0.6f))
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
