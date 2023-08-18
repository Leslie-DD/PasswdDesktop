package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Passwd(
    val comment: String?,
    val groupId: Int?,
    val id: Int,
    val link: String?,
    val password: String?,
    val passwordString: String?,
    val title: String?,
    val userId: Int?,
    val username: String?,
    val usernameString: String?
) {
}