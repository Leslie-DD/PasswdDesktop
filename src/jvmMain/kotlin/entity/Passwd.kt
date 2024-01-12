package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Passwd(
    var comment: String?,
    val groupId: Int,
    val id: Int,
    var link: String?,
    var usernameString: String?,
    var passwordString: String?,
    var title: String?,
    val userId: Int?,
    @SerialName("updateTimeExpose")
    var updateTime: Long? = 0L
) : IDragAndDrop


fun defaultPasswd(): Passwd = Passwd(
    comment = null,
    groupId = -1,
    id = -1,
    link = null,
    usernameString = null,
    passwordString = null,
    title = null,
    userId = -1
)