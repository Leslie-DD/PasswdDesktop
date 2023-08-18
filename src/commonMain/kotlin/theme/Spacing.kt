package theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val titleLineSpace: Dp = 15.dp,
    val moduleLineSpace: Dp = 30.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }