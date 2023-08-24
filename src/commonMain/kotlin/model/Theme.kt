package model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.WbAuto
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import model.Theme.Companion.Themes
import theme.Material3DarkTheme
import theme.Material3LightTheme

sealed interface Theme {

    /**
     * Material3的主题配置
     */
    val materialColorScheme: ColorScheme
        @Composable get

    val isLight: Boolean
        @Composable get

    val isDark: Boolean
        @Composable get

    val name: String

    val iconVector: ImageVector

    object Dark : Theme {
        override val materialColorScheme
            @Composable get() = Material3DarkTheme
        override val isLight
            @Composable get() = false
        override val isDark
            @Composable get() = !isLight

        override val name = "深色模式"

        override val iconVector: ImageVector = Icons.Default.ShieldMoon
    }

    object Light : Theme {
        override val materialColorScheme
            @Composable get() = Material3LightTheme
        override val isLight
            @Composable get() = true
        override val isDark
            @Composable get() = !isLight

        override val name = "亮色模式"

        override val iconVector: ImageVector = Icons.Default.WbSunny
    }

    object Auto : Theme {
        override val materialColorScheme
            @Composable get() = if (isDark) Dark.materialColorScheme else Light.materialColorScheme

        override val isLight
            @Composable get() = !isDark
        override val isDark
            @Composable get() = isSystemInDarkTheme()

        override val name = "自动模式"

        override val iconVector: ImageVector = Icons.Default.WbAuto
    }

    companion object {
        val Default = Auto
        val Themes = listOf(Auto, Light, Dark)
    }

}

fun Theme.next() = Themes[(Themes.indexOf(this) + 1) % Themes.size]
