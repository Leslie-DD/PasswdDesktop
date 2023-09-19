package model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface UiScreen {

    val name: String

    val icon: ImageVector

    companion object {
        val Default = Loading

        val Screens = listOf(Passwds, Settings)


        val LoginAndSignup = listOf(Login, Signup)
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
        override val name = "Login"
        override val icon = Icons.Default.Login
    }

    object Signup : UiScreen {
        override val name = "Sign up"
        override val icon = Icons.Default.AppRegistration
    }

    object Loading : UiScreen {
        override val name = "加载中"
        override val icon = Icons.Default.Downloading

    }


}