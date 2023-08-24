package passwds.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResult(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("secret_key")
    val secretKey: String,
    val token: String,
)
