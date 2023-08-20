package passwds.model

import passwds.entity.Group

sealed interface UiEffect {

    class NewGroupResult(val group: Group?) : UiEffect

    class DeleteGroupResult(val group: Group?) : UiEffect

    object PasswdScreen : UiEffect


}