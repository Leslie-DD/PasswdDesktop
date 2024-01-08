package database

import com.passwd.common.database.History
import database.entity.HistoryData

fun HistoryData.mapToHistory(): History {
    return History(
        id,
        userId,
        username,
        password,
        secretKey,
        host,
        port,
        accessToken,
        saved,
        silentlyLogin,
        createTime
    )
}

fun mapHistoryList(
    id: Int,
    userId: Int?,
    username: String?,
    password: String?,
    secretKey: String?,
    host: String?,
    port: Int?,
    accessToken: String?,
    saved: Boolean?,
    silentlyLogin: Boolean?,
    createTime: Long?,
): HistoryData {
    return HistoryData(
        id = id,
        userId = userId ?: 0,
        username = username ?: "",
        password = password ?: "",
        secretKey = secretKey ?: "",
        accessToken = accessToken ?: "",
        host = host ?: "",
        port = port ?: 8080,
        saved = saved ?: false,
        silentlyLogin = silentlyLogin ?: false,
        createTime = createTime ?: 0
    )
}