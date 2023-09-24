package utils

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
}
