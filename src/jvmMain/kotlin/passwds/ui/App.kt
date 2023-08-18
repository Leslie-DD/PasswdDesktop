package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen
import theme.LocalSpacing
import theme.Spacing

@Composable
fun PasswdApp(viewModel: PasswdsViewModel) {
    val theme by viewModel.theme.collectAsState()
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        androidx.compose.material.MaterialTheme(colors = theme.materialColors) {
            MaterialTheme(colorScheme = theme.materialColorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PasswdMainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswdMainScreen(viewModel: PasswdsViewModel) {
    val isLandscape = viewModel.uiState.isLandscape
    val coroutine = rememberCoroutineScope()
    val scaffoldState = viewModel.scaffoldState
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        drawerContent = if (!isLandscape) {
            {
                SideMenuScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            null
        },
        topBar = {
            if (!isLandscape) {
                TopAppBar(
                    title = {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(5.dp))
                            IconButton(onClick = {
                                coroutine.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Passwd",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Compose Multiplatform",
                                    modifier = Modifier,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            MainContentScreen(viewModel)
        }
    }
}

@Composable
fun MainContentScreen(viewModel: PasswdsViewModel) {
    val content: @Composable (UiScreen) -> Unit = {
        when (it) {
            UiScreen.Translate -> PasswdContentScreen(viewModel)
            UiScreen.Settings -> SettingsScreen(viewModel)
            else -> {}
        }
    }
    val uiState = viewModel.uiState
    if (uiState.isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            SideMenuScreen(viewModel = viewModel, modifier = Modifier.width(200.dp))
            Box(
                modifier = Modifier.width(2.dp).fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                Color.Transparent
                            )
                        ),
                        alpha = 0.6f
                    )
            )
            Crossfade(targetState = uiState.uiScreen, content = content)
        }
    } else {
        Crossfade(targetState = uiState.uiScreen, content = content)
    }
}

@Composable
fun SettingsScreen(viewModel: PasswdsViewModel) {
    Text("This is the SettingsScreen.")
}


