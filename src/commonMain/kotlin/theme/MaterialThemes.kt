package theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Material3LightTheme = lightColorScheme(
    surface = Color(0xFFf5f6f7),
    onSurface = Color(0xFFC6C7C7),

    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF2F2525),
    secondary = Color(0xFF81AEF4),
    secondaryContainer = Color(0xFFE5E6E7),
    onSecondaryContainer = Color(0xFFD9DADB),

    // toolbar background
    tertiary = Color(0xFFF3F4F6),

    tertiaryContainer = COLOR_SUBJECT_LIGHT,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFFEDEDED)
)

val Material3DarkTheme = darkColorScheme(
    surface = Color(0xFF232325),
    onSurface = Color(0xFF424345),

    primary = Color(0xFF353940),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1D1F20),
    onPrimaryContainer = Color(0xFFBBBEBC),
    secondary = Color(0xFF3477A2),
    secondaryContainer = Color(0xFF292A2C),
    onSecondaryContainer = Color(0xFF363739),

    // toolbar background
    tertiary = Color(0xFF292C30),

    tertiaryContainer = COLOR_SUBJECT_DARK,

    background = Color(0xFF2B2B2B),
    onBackground = Color(0xFF3E3E3E)
)
