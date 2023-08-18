package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val userId: Int,
    val groupName: String?,
    val groupComment: String?
)
