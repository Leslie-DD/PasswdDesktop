package entity

data class PasswdUser(
    val id: Int,
    val username: String,
    val secretKey: String? = null,
    val token: String
)
