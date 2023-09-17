package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import platform.desktop.storageDir
import java.io.File

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
}
