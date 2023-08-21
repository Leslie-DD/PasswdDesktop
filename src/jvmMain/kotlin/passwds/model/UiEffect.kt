package passwds.model

import passwds.entity.Group
import passwds.entity.Passwd

sealed interface UiEffect {

    class NewGroupResult(val group: Group?) : UiEffect

    class DeleteGroupResult(val group: Group?) : UiEffect

    class UpdateGroupResult(val group: Group?) : UiEffect

    class NewPasswdResult(val passwd: Passwd?) : UiEffect

    class UpdatePasswdResult(val passwd: Passwd?) : UiEffect

    class DeletePasswdResult(val passwd: Passwd?) : UiEffect

    object LoginAndRegisterSuccess : UiEffect

    class LoginAndRegisterFailure(val tipsMsg: String?) : UiEffect


}