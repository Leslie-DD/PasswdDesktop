package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

@Serializable
data class Group(
    val id: Int,
    val userId: Int,
    var groupName: String?,
    var groupComment: String?,
    @SerialName("updateTimeExpose")
    var updateTime: Long? = 0L
) : IDragAndDrop {

    override fun toString(): String {
        return "[$groupName]"
    }

    companion object {
        private val autoIncrementId = AtomicInteger(1)
        fun defaultGroup(): Group = Group(
            id = autoIncrementId.get(),
            userId = 1,
            groupName = "testGroupName ${autoIncrementId.get()}",
            groupComment = "test Group Comment ${autoIncrementId.getAndIncrement()}"
        )
    }
}
