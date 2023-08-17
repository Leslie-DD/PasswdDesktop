package network.entity

import kotlinx.serialization.Serializable

@Serializable
data class KtorResult<T>(
    val success: Boolean,
    val code: Int,
    val msg: String,
    val data: T
) {
    fun result(): Result<T> {
        return if (success) {
            Result.success(data)
        } else {
            Result.failure(Throwable(msg))
        }
    }
}
