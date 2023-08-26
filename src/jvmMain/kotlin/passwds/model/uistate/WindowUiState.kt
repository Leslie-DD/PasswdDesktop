package passwds.model.uistate

import passwds.model.UiScreen
import platform.desktop.Platform
import platform.desktop.currentPlatform

data class WindowUiState(
    val windowVisible: Boolean,
    val isLandscape: Boolean,
    val uiScreen: UiScreen,

    val menuOpen: Boolean = true,
) {
    companion object {
        val Default = WindowUiState(
            windowVisible = true,
            isLandscape = currentPlatform == Platform.Desktop,
            uiScreen = UiScreen.Default,
        )
    }
}