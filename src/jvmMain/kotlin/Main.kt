import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.Res
import passwds.entity.Passwd
import passwds.model.PasswdsViewModel
import passwds.ui.MainScreen

@Composable
fun PasswdCard(passwd: Passwd) {
    Column(modifier = Modifier.fillMaxSize()) {
//        var title by remember { mutableStateOf(passwd.title) }
//        OutlinedTextField(
//            value = title ?: "",
//            label = { Text(title ?: "") },
//            onValueChange = { title = it },
//            modifier = Modifier.weight(1f)
//        )
        Text(text = passwd.title ?: "")
        Spacer(Modifier.width(20.dp))
        Text(text = passwd.passwordString ?: "")

    }
}


fun main() = application {

    val state = rememberWindowState(width = 1000.dp)
    val viewModel = remember { PasswdsViewModel() }
    if (viewModel.exitApp.collectAsState().value) {
        exitApplication()
    }
    viewModel.shouldBeLandscape.tryEmit(!state.size.isLandscape)
    //这是一个托盘
    Tray(
        icon = painterResource(Res.drawable.app_icon_round_corner),
        onAction = {
            viewModel.updateUiState { copy(windowVisible = true) }
        },
        tooltip = "双击(windows)\\右击(mac)打开翻译器",
    ) {
        Item("Open Window", onClick = {viewModel.updateUiState { copy(windowVisible = true) }})
        Item("Exit App", onClick = ::exitApplication)
    }
    Window(
        onCloseRequest = { viewModel.updateUiState { copy(windowVisible = false) } },
        visible = viewModel.uiState.windowVisible,
        title = "Passwd",
        state = state
    ) {
        val passwds = viewModel.passwds.collectAsState()
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                MainScreen(viewModel)
            }

//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(passwds.value) {
//                    PasswdCard(it)
//                }
//            }
        }
    }
}

/**
 * 认为 4:3 是区分横竖屏的分界点
 */
private val DpSize.isLandscape: Boolean
    get() = (height / width) > (4f / 3f)
