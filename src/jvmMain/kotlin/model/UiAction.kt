package model

import entity.Group
import entity.Passwd

sealed class UiAction {

    class WindowVisible(val visible: Boolean) : UiAction()

    class GoScreen(val screen: UiScreen) : UiAction()

    class ShowGroupPasswds(val groupId: Int) : UiAction()

    class ShowPasswd(val passwdId: Int) : UiAction()

    class Login(
        val username: String,
        val password: String,
        val secretKey: String,
        val saved: Boolean,
        val silentlySignIn: Boolean
    ) : UiAction()

    class Register(val username: String, val password: String) : UiAction()

    class NewGroup(val groupName: String, val groupComment: String) : UiAction()

    object ClearEffect : UiAction()

    object DeleteGroup : UiAction()

    class UpdateGroup(val groupName: String, val groupComment: String) : UiAction()

    class NewPasswd(
        val groupId: Int,
        val title: String,
        val usernameString: String,
        val passwordString: String,
        val link: String,
        val comment: String
    ) : UiAction()

    class UpdatePasswd(val passwd: Passwd) : UiAction()

    object DeletePasswd : UiAction()

    class MenuOpenOrClose(val open: Boolean) : UiAction()

    class SearchPasswds(val content: String) : UiAction()

    class ReorderGroupDragEnd(val reorderedGroupList: MutableList<Group>) : UiAction()

}