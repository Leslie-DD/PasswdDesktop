package model.uistate

import model.uieffect.DialogUiEffect

data class DialogUiState(
    val effect: DialogUiEffect? = null
) {
    companion object {
        fun defaultDialogUiState(): DialogUiState = DialogUiState()
    }
}