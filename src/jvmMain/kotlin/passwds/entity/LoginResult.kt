package passwds.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResult(
    @SerialName("user_id")
    val userId: Int,
    val username: String,
    val passwds: MutableList<Passwd>,
    val token: String
)