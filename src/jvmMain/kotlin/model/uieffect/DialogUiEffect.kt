package model.uieffect

import entity.Group
import entity.Passwd

sealed interface DialogUiEffect {

    class NewGroupResult(val group: Group?) : DialogUiEffect

    class DeleteGroupResult(val group: Group?) : DialogUiEffect

    class UpdateGroupResult(val group: Group?) : DialogUiEffect

    class NewPasswdResult(val passwd: Passwd?) : DialogUiEffect

    class UpdatePasswdResult(val passwd: Passwd?) : DialogUiEffect

    class DeletePasswdResult(val passwd: Passwd?) : DialogUiEffect

    class LoginAndSignupFailure(val tipsMsg: String?) : DialogUiEffect

    class SignupResult(val secretKey: String) : DialogUiEffect

}