package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Passwd(
    var comment: String?,
    val groupId: Int?,
    val id: Int,
    var link: String?,
    var usernameString: String?,
    var passwordString: String?,
    val username: ByteArray? = null,
    var password: ByteArray? = null,
    var title: String?,
    val userId: Int?,
) {
//    fun copy() = Passwd(comment, groupId, id, link, usernameString, passwordString, username, password, title, userId)
}