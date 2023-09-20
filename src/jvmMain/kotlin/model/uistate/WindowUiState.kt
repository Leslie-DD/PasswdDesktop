package model.uistate

import model.UiScreen

data class WindowUiState(
    val windowVisible: Boolean,
    val uiScreen: UiScreen,

    val menuOpen: Boolean = false,
) {
    companion object {
        val Default = WindowUiState(
            windowVisible = true,
            uiScreen = UiScreen.Default,
        )
    }
}