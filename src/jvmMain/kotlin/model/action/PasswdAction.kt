package model.action

import entity.Group
import entity.Passwd
import model.UiScreen

sealed class PasswdAction {

    class GoScreen(val screen: UiScreen) : PasswdAction()

    class ShowGroupPasswds(val groupId: Int) : PasswdAction()

    class ShowPasswd(val passwdId: Int) : PasswdAction()

    class NewGroup(val groupName: String, val groupComment: String) : PasswdAction()

    object ClearEffect : PasswdAction()

    object DeleteGroup : PasswdAction()

    class UpdateGroup(val groupName: String, val groupComment: String) : PasswdAction()

    class NewPasswd(
        val groupId: Int,
        val title: String,
        val usernameString: String,
        val passwordString: String,
        val link: String,
        val comment: String
    ) : PasswdAction()

    class UpdatePasswd(val passwd: Passwd) : PasswdAction()

    object DeletePasswd : PasswdAction()

    class MenuOpenOrClose(val open: Boolean) : PasswdAction()

    class SearchPasswds(val content: String) : PasswdAction()

    class ReorderGroupDragEnd(val reorderedGroupList: MutableList<Group>) : PasswdAction()

    class ExportPasswdsToFile(val filePath: String) : PasswdAction()

    class InitHost(val host: Pair<String, Int>) : PasswdAction()

    class UpdateEditEnabled(val editEnabled: Boolean) : PasswdAction()

}
