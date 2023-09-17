package model.uistate

import entity.Group
import model.uieffect.GroupUiEffect

data class GroupUiState(
    val groups: MutableList<Group>,
    val selectGroup: Group? = null,
    val uiEffect: GroupUiEffect? = null
) {
    companion object {
        fun defaultGroupUiState(): GroupUiState = GroupUiState(
            groups = arrayListOf()
        )
    }
}
