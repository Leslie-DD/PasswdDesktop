package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class LoginResult(
    val user_id: Int,
    val username: String,
    val passwds: MutableList<Passwd>,
    val token: String
)