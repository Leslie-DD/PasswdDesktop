package model.uistate

import model.UiScreen
import model.UiScreens

data class WindowUiState(
    val uiScreen: UiScreen,
    val uiScreens: UiScreens,

    val menuOpen: Boolean = false,
) {
    companion object {
        val Default = WindowUiState(
            uiScreen = UiScreen.Default,
            uiScreens = UiScreen.Loadings,
        )
    }
}