package passwds.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import passwds.model.PasswdsViewModel
import passwds.model.TranslateScreenUiAction
import passwds.model.UiScreen


@Composable
fun SideMenuScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier
) {

    val coroutine = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxSize()) {
        val uiScreen = viewModel.uiState.uiScreen
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Box {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp).background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    Color.Transparent,
                                )
                            )
                        )
                    )
                    if (!viewModel.uiState.isLandscape) {
                        Box(modifier = Modifier.padding(20.dp)) {
                            IconButton(onClick = {
                                coroutine.launch {
                                    viewModel.scaffoldState.drawerState.close()
                                }
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))
//                        Image(src = Res.drawable.app_icon_round_corner, modifier = Modifier.size(45.dp))
                        Spacer(modifier = Modifier.height(30.dp))
                        androidx.compose.material.Text(text = "Passwd", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material.Text(text = "Compose for Multiplatform", fontSize = 12.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            ScreensListMenu(viewModel, uiScreen)

            item { Spacer(modifier = Modifier.height(80.dp)) }

//            item {
//                val theme by viewModel.theme.collectAsState()
//                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//                    androidx.compose.material.TextButton(onClick = {
//                        viewModel.theme.tryEmit(theme.next())
//                    }) {
//                        AnimatedContent(
//                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp).alpha(0.5f),
//                            targetState = theme,
//                            transitionSpec = {
//                                fadeIn() + slideInHorizontally() with fadeOut() + slideOutVertically()
//                            }) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.animateContentSize()
//                            ) {
//                                theme.iconVector?.let { icon ->
//                                    Icon(
//                                        imageVector = icon,
//                                        contentDescription = null
//                                    )
//                                }
//                                Spacer(modifier = Modifier.width(10.dp))
//                                Text(text = theme.name)
//                            }
//                        }
//                    }
//                }
//            }


        }




        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            androidx.compose.material.TextButton(
                onClick = {
                    viewModel.onAction(TranslateScreenUiAction.ExitApp)
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
                Text(text = "退出应用")
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

fun LazyListScope.ScreensListMenu(
    viewModel: PasswdsViewModel,
    currentScreen: UiScreen,
) {
    items(UiScreen.Screens) { screen ->
        val isSelected = screen == currentScreen
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = 10.dp, top = 5.dp, bottom = 5.dp),
            contentAlignment = Alignment.Center
        ) {
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

            val coroutine = rememberCoroutineScope()
            androidx.compose.material.TextButton(
                modifier = Modifier.fillMaxWidth()
                    .graphicsLayer(clip = true, shape = RoundedCornerShape(bottomEndPercent = 50, topEndPercent = 50)),
                onClick = {
                    viewModel.onAction(TranslateScreenUiAction.GoScreen(screen))
                    coroutine.launch {
                        viewModel.scaffoldState.drawerState.close()
                    }
                },
                colors = androidx.compose.material.ButtonDefaults.textButtonColors(contentColor = if (isSelected) androidx.compose.material.MaterialTheme.colors.primary else MaterialTheme.colorScheme.outline)
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