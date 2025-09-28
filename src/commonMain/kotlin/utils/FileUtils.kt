package utils

import entity.Passwd
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import platform.desktop.storageDir
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object FileUtils {

    private val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    fun getFileInUserHome(fileName: String): File {
        val dir = storageDir
        if (!dir.isDirectory || !dir.exists()) {
            val mkdirResult = dir.mkdir()
            logger.info(("mkdir dir: ${dir.absoluteFile}, result: $mkdirResult"))
        }
        return File(System.getProperty("user.home"), "Passwd/$fileName")
    }

    fun exportDataToFile(filePath: String, data: String) {
        logger.info("exportDataToFile filePath: $filePath")
        var bufferWriter: BufferedWriter? = null
        try {
            val exportFile = File(filePath).apply {
                if (!exists()) {
                    createNewFile()
                }
            }
            bufferWriter = BufferedWriter(FileWriter(exportFile))
            bufferWriter.write(data)
        } catch (e: Exception) {
            logger.error("exportDataToFile: error filePath: $filePath, ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                bufferWriter?.close()
            } catch (_: Exception) {
            }
        }
    }


    /**
     * 从JSON字符串导入密码数据，忽略分组名称和其他字段
     * @param jsonString JSON格式的密码数据
     * @return 解析后的Passwd对象列表
     */
    fun importFromJsonString(jsonString: String): Map<String, List<Passwd>> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(jsonString)
    }

    /**
     * 从JSON文件导入密码数据，忽略分组名称和其他字段
     * @param filePath JSON文件路径
     * @return 解析后的Passwd对象列表
     */
    fun importFromJsonFile(filePath: String): Map<String, List<Passwd>> {
        val jsonString = File(filePath).readText()
        return importFromJsonString(jsonString)
    }
}
