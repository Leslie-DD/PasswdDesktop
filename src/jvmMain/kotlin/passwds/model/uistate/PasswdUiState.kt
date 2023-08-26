package passwds.model.uistate

import passwds.entity.Passwd

data class PasswdUiState(
//    val passwdsMap: MutableMap<Int, MutableList<Passwd>>,
    val groupPasswds: MutableList<Passwd>,
    val selectPasswd: Passwd? = null,
) {
    companion object {
        fun defaultPasswdUiState(): PasswdUiState = PasswdUiState(
//            passwdsMap = hashMapOf(),
            groupPasswds = arrayListOf()
        )
    }
}
