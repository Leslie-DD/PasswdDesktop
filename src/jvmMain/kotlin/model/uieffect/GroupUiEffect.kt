package model.uieffect

import entity.Group

sealed interface GroupUiEffect {

    class GroupListUpdated(val updateGroups: MutableList<Group>) : GroupUiEffect
}