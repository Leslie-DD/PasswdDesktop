package passwds.model

sealed class UiAction {

    class OnInputChanged(val newText: String) : UiAction()

    object DoTranslate : UiAction()

    class ChangeMenuVisible(val open: Boolean, val isSrc: Boolean) : UiAction()

    object ClearAll : UiAction()

    object OnCopy : UiAction()

    /**
     * 关闭此应用
     */
    object ExitApp : UiAction()

    object Logout : UiAction()

    class WindowVisible(val visible: Boolean) : UiAction()

    class GoScreen(val screen: UiScreen) : UiAction()

    class ShowGroupPasswds(val groupId: Int) : UiAction()

    class ShowPasswd(val passwdId: Int) : UiAction()

    class Login(val username: String, val password: String, val secretKey: String) : UiAction()

}
