package model.uistate

import entity.Group
import entity.Passwd

data class PasswdUiState(
    val groups: MutableList<Group>,
    val selectGroup: Group? = null,
    val groupPasswds: MutableList<Passwd>,
    val selectPasswd: Passwd? = null,
    val editEnabled: Boolean = false,
) {
    companion object {
        fun defaultPasswdUiState(): PasswdUiState = PasswdUiState(
            groups = arrayListOf(),
            groupPasswds = arrayListOf()
        )
    }
}
