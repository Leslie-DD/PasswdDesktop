package passwds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import passwds.model.PasswdsViewModel

@Composable
fun LoginScreen(
    viewModel: PasswdsViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppSymbolBox()
            Spacer(modifier = Modifier.height(30.dp))

            Username(viewModel = viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            Password(viewModel = viewModel)
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Username(viewModel: PasswdsViewModel) {
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text("Username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.People, contentDescription = null)
        },
        value = "",
        maxLines = 1,
        singleLine = true,
        onValueChange = {},
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Password(viewModel: PasswdsViewModel) {
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        enabled = true,
        label = { Text("Password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
        },
        value = "",
        maxLines = 1,
        singleLine = true,
        onValueChange = {},
    )
}