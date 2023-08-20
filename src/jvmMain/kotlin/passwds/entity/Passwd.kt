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
    val username: ByteArray? = null,
    val password: ByteArray? = null,
    val title: String?,
    val userId: Int?,
)