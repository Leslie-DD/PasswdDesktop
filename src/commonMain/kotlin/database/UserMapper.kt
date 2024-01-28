package database

import com.passwd.common.database.Passwd
import com.passwd.common.database.Passwd_user
import database.entity.UserData

fun UserData.mapToPasswdUser(): Passwd_user {
    return Passwd_user(
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
        createTime,
    )
}

fun mapToUserData(
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
): UserData = UserData(
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
