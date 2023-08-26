package passwds.model.uistate

import passwds.model.DialogUiEffect

data class DialogUiState(
    val effect: DialogUiEffect? = null
) {
    companion object {
        fun defaultDialogUiState(): DialogUiState = DialogUiState()
    }
}