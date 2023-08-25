package theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Material3LightTheme = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF2F2525),
    primaryContainer = Color(0xFFf4f7f0),
    onPrimaryContainer = Color(0xFF2F2525),
)

val Material3DarkTheme = darkColorScheme(
    primary = Color(0xFF353940),
    onPrimary = Color(0xFF373737),
    primaryContainer = Color(0xFF2D2F31),
    onPrimaryContainer = Color(0xFFBBBEBC),
)
