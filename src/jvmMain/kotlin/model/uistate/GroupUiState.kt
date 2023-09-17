package model.uistate

import entity.Group

data class GroupUiState(
    val groups: MutableList<Group>,
    val selectGroup: Group? = null,
) {
    companion object {
        fun defaultGroupUiState(): GroupUiState = GroupUiState(
            groups = arrayListOf()
        )
    }
}
