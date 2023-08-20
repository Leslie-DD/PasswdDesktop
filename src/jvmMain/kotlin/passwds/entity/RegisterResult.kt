package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResult(
    val user_id: Int,
    val secret_key: String,
    val token: String,
)
