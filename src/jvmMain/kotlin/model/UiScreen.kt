package model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

typealias UiScreens = List<UiScreen>

sealed interface UiScreen {

    val name: String

    val icon: ImageVector

    companion object {
        val Default = Loading

        val Loadings: UiScreens = listOf(Loading)

        val LoginAndSignup: UiScreens = listOf(Login, Signup)

        val LoggedInScreen: UiScreens = listOf(Passwds, Settings)
    }

    object Passwds : UiScreen {
        override val name = "Passwds"
        override val icon = Icons.Default.Lock
    }

    object Settings : UiScreen {
        override val name = "Settings"
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