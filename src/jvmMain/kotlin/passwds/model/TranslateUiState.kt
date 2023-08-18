package passwds.model

import passwds.entity.Group
import passwds.entity.Passwd
import platform.desktop.Platform
import platform.desktop.currentPlatform

data class TranslateUiState(
    val swapLangButtonState: Boolean,
    val menu1Open: Boolean,
    val menu2Open: Boolean,
    val textFieldError: Boolean,
    val windowVisible: Boolean,
    val isLandscape: Boolean,
    val uiScreen: UiScreen,

    val groups: List<Group>,
    val groupPasswds: List<Passwd>,
    val passwds: List<Passwd>,

    val selectGroup: Group? = null,
    val selectPasswd: Passwd? = null
) {
    companion object {
        val Default = TranslateUiState(
            swapLangButtonState = true,
            menu1Open = false,
            menu2Open = false,
            textFieldError = false,
            windowVisible = true,
            isLandscape = currentPlatform == Platform.Desktop,
            uiScreen = UiScreen.Default,

            groups = emptyList(),
            groupPasswds = emptyList(),
            passwds = emptyList(),
        )
    }
}