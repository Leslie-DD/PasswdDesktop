package model.uistate

import database.entity.UserData

data class LoginUiState(
    val userData: UserData,
    val userDataList: List<UserData>
)