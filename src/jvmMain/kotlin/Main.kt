import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import passwds.entity.Passwd
import passwds.model.PasswdsViewModel

@Composable
fun App() {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("Hello, World!") }

    val viewModel = remember { PasswdsViewModel() }
    val passwds = viewModel.passwds.collectAsState()
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Button(onClick = {
                text = "开始请求。。。"
                scope.launch {
                    viewModel.fetchPasswds()
                }
            }) {
                Text("${passwds.value.size}")
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(passwds.value) {
                    PasswdCard(it)
                }
            }
        }
    }
}

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
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
