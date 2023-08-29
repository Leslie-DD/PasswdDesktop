package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val userId: Int,
    var groupName: String?,
    var groupComment: String?
)
