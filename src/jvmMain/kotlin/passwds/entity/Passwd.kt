package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Passwd(
    val comment: String?,
    val groupId: Int?,
    val id: Int,
    val link: String?,
    val usernameString: String?,
    val passwordString: String?,
    val username: ByteArray?,
    val password: ByteArray?,
    val title: String?,
    val userId: Int?,
)