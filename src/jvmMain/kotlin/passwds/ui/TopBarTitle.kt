package passwds.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.Res
import model.next
import passwds.model.PasswdsViewModel
import passwds.model.UiScreen

@Composable
fun TopBarTitle(
    uiScreen: UiScreen,
    coroutine: CoroutineScope,
    scaffoldState: ScaffoldState,
    viewModel: PasswdsViewModel
) {
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

                if (uiScreen is UiScreen.Passwds) {
                    SearchBox()
                }
            }

            ThemeChoiceButton(viewModel)
            Spacer(modifier = Modifier.width(10.dp))
            MenuBox(coroutine, scaffoldState)
        }
        Divider(modifier = Modifier.padding(top = 5.dp, end = 10.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ThemeChoiceButton(viewModel: PasswdsViewModel) {
    val theme by viewModel.theme.collectAsState()
    Box(
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = {
                viewModel.theme.tryEmit(theme.next())
            }
        ) {
            AnimatedContent(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp).alpha(0.5f),
                targetState = theme,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() with fadeOut() + slideOutVertically()
                }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {
                    theme.iconVector?.let { icon ->
                        androidx.compose.material.Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    androidx.compose.material3.Text(text = theme.name)
                }
            }
        }
    }
}

@Composable
private fun MenuBox(
    coroutine: CoroutineScope,
    scaffoldState: ScaffoldState
) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBox() {
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