package passwds.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface UiScreen {

    val name: String

    val icon: ImageVector

    companion object {
        val Default = Translate

        val Screens = listOf(Translate, Settings)
    }

    object Translate : UiScreen {
        override val name = "密码"
        override val icon = Icons.Default.Lock
    }

    object Settings : UiScreen {
        override val name = "设置"
        override val icon = Icons.Default.Settings
    }

}