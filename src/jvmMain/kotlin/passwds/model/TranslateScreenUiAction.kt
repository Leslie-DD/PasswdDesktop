package passwds.model

sealed class TranslateScreenUiAction {

    class OnInputChanged(val newText: String) : TranslateScreenUiAction()

    object DoTranslate : TranslateScreenUiAction()

    class ChangeMenuVisible(val open: Boolean, val isSrc: Boolean) : TranslateScreenUiAction()

    object ClearAll : TranslateScreenUiAction()

    object OnCopy : TranslateScreenUiAction()

    /**
     * 关闭此应用
     */
    object ExitApp : TranslateScreenUiAction()

    class GoScreen(val screen: UiScreen) : TranslateScreenUiAction()

}
