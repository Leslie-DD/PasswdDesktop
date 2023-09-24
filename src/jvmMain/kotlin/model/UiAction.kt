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
        val host: String,
        val port: Int,
        val saved: Boolean,
        val silentlyLogin: Boolean
    ) : UiAction()

    class Signup(
        val username: String,
        val password: String,
        val host: String,
        val port: Int,
    ) : UiAction()

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

    class ExportPasswdsToFile(val filePath: String) : UiAction()

    class InitHost(val host: Pair<String, Int>) : UiAction()

}
