package model.uistate

import model.UiScreen
import platform.Platform
import platform.desktop.currentPlatform

data class WindowUiState(
    val windowVisible: Boolean,
    val isLandscape: Boolean,
    val uiScreen: UiScreen,

    val menuOpen: Boolean = false,
) {
    companion object {
        val Default = WindowUiState(
            windowVisible = true,
            isLandscape = currentPlatform == Platform.Desktop,
            uiScreen = UiScreen.Default,
        )
    }
}