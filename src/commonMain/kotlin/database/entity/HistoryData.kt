package database.entity

import kotlinx.datetime.Clock

data class HistoryData(
    val id: Int = 0,
    val userId: Int = 0,
    val username: String,
    val password: String,
    val secretKey: String,
    val accessToken: String,
    val saved: Boolean = false,
    val silentlyLogin: Boolean = false,
    val createTime: Long = Clock.System.now().epochSeconds
) {
    companion object {
        fun defaultHistoryData(): HistoryData {
            return HistoryData(
                username = "",
                password = "",
                secretKey = "",
                accessToken = ""
            )
        }
    }
}
