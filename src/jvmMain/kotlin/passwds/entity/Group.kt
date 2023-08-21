package passwds.entity

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val userId: Int,
    var groupName: String?,
    var groupComment: String?
) {
    companion object {
        const val GROUP_PARAM_NAME = "group_name"
        const val GROUP_PARAM_COMMENT = "group_comment"
    }
}
