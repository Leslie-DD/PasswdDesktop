package passwds.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import model.next
import passwds.model.PasswdsViewModel
import passwds.model.UiAction
import passwds.model.UiScreen

@Composable
fun SideMenuScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {

    val coroutine = rememberCoroutineScope()

    val expand = viewModel.uiState.menuOpen
    Column(
        modifier = modifier
            .width(if (expand) 200.dp else 70.dp)
            .fillMaxHeight()
            .padding(top = 20.dp, bottom = 40.dp, start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {

        val uiScreen = viewModel.uiState.uiScreen
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expand) {
                        Text(text = "Passwd", fontSize = 20.sp, color = Color(0xffa9c8fc))
                    }
                    IconButton(
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
                Spacer(modifier = Modifier.height(15.dp))

                val isSelected = screen == uiScreen
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
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                ) {
                    Icon(imageVector = screen.icon, contentDescription = null)
                    if (expand) {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(text = screen.name, fontSize = 18.sp)
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
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    viewModel.onAction(UiAction.GoScreen(UiScreen.Login))
                    coroutine.launch {
                        viewModel.scaffoldState.drawerState.close()
                    }
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
            viewModel.theme.tryEmit(theme.next())
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

/**
 * 无点击效果 InteractionSource
 */
class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}
