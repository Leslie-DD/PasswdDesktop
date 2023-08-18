package passwds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch
import model.Res
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen
import theme.LocalSpacing
import theme.Spacing

@Composable
fun PasswdApp(
    viewModel: PasswdsViewModel,
    state: WindowState
) {
    val theme by viewModel.theme.collectAsState()
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        androidx.compose.material.MaterialTheme(colors = theme.materialColors) {
            MaterialTheme(colorScheme = theme.materialColorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PasswdMainScreen(viewModel, state)
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswdMainScreen(
    viewModel: PasswdsViewModel,
    state: WindowState
) {
    val coroutine = rememberCoroutineScope()
    val scaffoldState = viewModel.scaffoldState
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        drawerContent = {
            SideMenuScreen(
                viewModel = viewModel,
                modifier = Modifier.width(200.dp)
            )
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth()) {

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Image(
                                    painter = painterResource(Res.Drawable.APP_ICON_ROUND_CORNER),
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
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
                                Spacer(modifier = Modifier.width(30.dp))

                                Box(modifier = Modifier.width(250.dp).height(50.dp)) {
                                    val text = remember { mutableStateOf("") }
                                    TextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(30),
                                        textStyle = TextStyle(fontSize = 14.sp),
                                        maxLines = 1,
                                        placeholder = {
                                            Text(
                                                text = "输入对应 Title 查找",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Light,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null
                                            )
                                        },
                                        value = text.value,
                                        onValueChange = {
                                            text.value = it
                                        },
                                        colors = TextFieldDefaults.textFieldColors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }

                            Row(modifier = Modifier.wrapContentSize()) {
                                IconButton(
                                    onClick = {
                                        coroutine.launch {
                                            scaffoldState.drawerState.open()
                                        }
                                    }) {
                                    Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }
                        Divider(modifier = Modifier.padding(top = 5.dp, end = 10.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
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
        }
    }
    Crossfade(targetState = viewModel.uiState.uiScreen, content = content)
}

@Composable
fun SettingsScreen(viewModel: PasswdsViewModel) {
    Text("This is the SettingsScreen.")
}


