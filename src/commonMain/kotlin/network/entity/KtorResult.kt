package network.entity

import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable
data class KtorResult<T>(
    val success: Boolean,
    val code: Int,
    val msg: String,
    val data: T,
    val timestamp: Long
) {
    var api: String? = null

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Companion::class.java)
    }

    fun result(): Result<T> {
        return if (success) {
            logger.debug("($api) success")
            Result.success(data)
        } else {
            logger.error("($api) failure $this")
            Result.failure(Throwable(msg))
        }
    }
}
