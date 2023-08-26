package passwds.model

import passwds.entity.Group
import passwds.entity.Passwd
import platform.desktop.Platform
import platform.desktop.currentPlatform

data class UiState(
    val swapLangButtonState: Boolean,
    val menu1Open: Boolean,
    val menu2Open: Boolean,
    val textFieldError: Boolean,
    val windowVisible: Boolean,
    val isLandscape: Boolean,
    val uiScreen: UiScreen,

    val groups: MutableList<Group>,
    val groupPasswds: MutableList<Passwd>,
    val passwds: List<Passwd>,

    val selectGroup: Group? = null,
    val selectPasswd: Passwd? = null,

    val effect: UiEffect? = null,
    val menuOpen: Boolean = true,
) {
    companion object {
        val Default = UiState(
            swapLangButtonState = true,
            menu1Open = false,
            menu2Open = false,
            textFieldError = false,
            windowVisible = true,
            isLandscape = currentPlatform == Platform.Desktop,
            uiScreen = UiScreen.Default,

            groups = arrayListOf(),
            groupPasswds = arrayListOf(),
            passwds = emptyList(),
        )
    }
}