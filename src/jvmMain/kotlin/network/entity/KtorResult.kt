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
//            try {
//                val databasePath = File(System.getProperty("compose.application.resources.dir"), "passwd_log.log")
//                if (!databasePath.exists()) {    //文件不存在则创建文件，先创建目录
//                    File(databasePath.parent).mkdirs()
//                    databasePath.createNewFile()
//                }
//                val outStream = FileOutputStream(databasePath)    //文件输出流用于将数据写入文件
//                val sourceByte: ByteArray = this.toString().toByteArray()
//                outStream.write(sourceByte)
//                outStream.close()    //关
//            } catch (exception: Exception) {
//                logger.info("($api) logfile printerror $this")
//            }
            logger.info("($api) success $this")
            Result.success(data)
        } else {
            logger.error("($api) failure $this")
            Result.failure(Throwable(msg))
        }
    }
}
