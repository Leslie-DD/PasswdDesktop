package database.entity

import kotlinx.datetime.Clock

data class UserData(
    val id: Int = 0,
    val userId: Int = 0,
    val username: String,
    val password: String,
    val secretKey: String,
    val host: String,
    val port: Int,
    val accessToken: String,
    val saved: Boolean = false,
    val silentlyLogin: Boolean = false,
    val createTime: Long = Clock.System.now().epochSeconds
) {
    companion object {
        fun defaultUserData(): UserData {
            return UserData(
                username = "",
                password = "",
                secretKey = "",
                host = "",
                port = 8080,
                accessToken = ""
            )
        }
    }
}
