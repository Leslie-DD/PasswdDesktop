package model.uistate

import model.UiScreen
import model.UiScreens

data class WindowUiState(
    val windowVisible: Boolean,
    val searchFocus: Boolean,
    val uiScreen: UiScreen,
    val uiScreens: UiScreens,

    val menuOpen: Boolean = false,
) {
    companion object {
        val Default = WindowUiState(
            windowVisible = true,
            searchFocus = true,
            uiScreen = UiScreen.Default,
            uiScreens = UiScreen.Loadings,
        )
    }
}