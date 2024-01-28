package database

import com.passwd.common.database.Passwd


fun entity.Passwd.mapToTablePasswd(
    syncd_to_cloud: Boolean? = true
): Passwd = Passwd(
    id,
    userId,
    groupId,
    title,
    usernameString,
    passwordString,
    link,
    comment,
    updateTime,
    syncd_to_cloud,
)

fun mapToPasswd(
    id: Int,
    userId: Int?,
    groupId: Int?,
    title: String?,
    usernameString: String?,
    passwordString: String?,
    link: String?,
    comment: String?,
    updateTime: Long?,
    syncd_to_cloud: Boolean? = false
): entity.Passwd = entity.Passwd(
    id = id,
    userId = userId ?: 0,
    groupId = groupId ?: 0,
    title = title ?: "",
    usernameString = usernameString ?: "",
    passwordString = passwordString ?: "",
    link = link ?: "",
    comment = comment ?: "",
    updateTime = updateTime ?: 0,
)