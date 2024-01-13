package datasource.user

import kotlinx.coroutines.flow.MutableStateFlow

object UserMemoryDataSource {

    val globalUserId = MutableStateFlow(-1)
    val globalUsername = MutableStateFlow("")
    val globalSecretKey = MutableStateFlow("")
    val globalAccessToken = MutableStateFlow("")

    suspend fun updateGlobalValues(
        secretKey: String,
        userId: Int,
        username: String,
        token: String
    ) {
        globalSecretKey.emit(secretKey)
        globalUserId.emit(userId)
        globalUsername.emit(username)
        globalAccessToken.emit(token)
    }
}