package database

import com.passwd.common.database.Passwd_group


fun entity.Group.mapToTableGroup(
    syncd_to_cloud: Boolean? = true
): Passwd_group = Passwd_group(
    id,
    userId,
    groupName,
    groupComment,
    updateTime,
    syncd_to_cloud
)

fun mapToGroup(
    id: Int,
    user_id: Int?,
    group_name: String?,
    group_comment: String?,
    update_time: Long?,
    syncd_to_cloud: Boolean? = false
): entity.Group = entity.Group(
    id = id,
    userId = user_id ?: 0,
    groupName = group_name ?: "",
    groupComment = group_comment ?: "",
    updateTime = update_time ?: 0,
)