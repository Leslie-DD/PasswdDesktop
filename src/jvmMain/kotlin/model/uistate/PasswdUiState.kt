package model.uistate

import entity.Passwd

data class PasswdUiState(
    val groupPasswds: MutableList<Passwd>,
    val selectPasswd: Passwd? = null,
) {
    companion object {
        fun defaultPasswdUiState(): PasswdUiState = PasswdUiState(
            groupPasswds = arrayListOf()
        )
    }
}
