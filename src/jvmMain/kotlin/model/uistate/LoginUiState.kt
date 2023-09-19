package model.uistate

import database.entity.HistoryData

data class LoginUiState(
    val historyData: HistoryData,
    val historyDataList: List<HistoryData>
)