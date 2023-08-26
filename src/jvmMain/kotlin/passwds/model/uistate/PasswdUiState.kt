package passwds.model.uistate

import passwds.entity.Passwd

data class PasswdUiState(
    val passwds: List<Passwd>,
    val groupPasswds: MutableList<Passwd>,
    val selectPasswd: Passwd? = null,
) {
    companion object {
        fun defaultPasswdUiState(): PasswdUiState = PasswdUiState(
            passwds = emptyList(),
            groupPasswds = arrayListOf()
        )
    }
}
