package passwds.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface UiScreen {

    val name: String

    val icon: ImageVector

    companion object {
        val Default = Loading

        val Screens = listOf(Passwds, Settings)


        val LoginAndRegister = listOf(Login, Register)
    }

    object Passwds : UiScreen {
        override val name = "密码"
        override val icon = Icons.Default.Lock
    }

    object Settings : UiScreen {
        override val name = "设置"
        override val icon = Icons.Default.Settings
    }

    object Login : UiScreen {
        override val name = "Sign in"
        override val icon = Icons.Default.Login
    }

    object Register : UiScreen {
        override val name = "Sign up"
        override val icon = Icons.Default.AppRegistration
    }

    object Loading : UiScreen {
        override val name = "加载中"
        override val icon = Icons.Default.Downloading

    }


}