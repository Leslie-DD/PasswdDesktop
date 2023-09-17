package platform.desktop

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.passwd.common.database.HistoryDatabase
import network.KtorRequest
import platform.Platform
import utils.FileUtils
import java.io.File

actual val currentPlatform: Platform = Platform.Desktop

actual val storageDir: File = File(System.getProperty("user.home"), "Passwd/")

actual fun createSqlDriver(): SqlDriver {
    val databasePath = FileUtils.getFileInUserHome("HistoryDatabase.db")
    KtorRequest.logger.info("HistoryDatabase.db absolute path: ${databasePath.absolutePath}")
    return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}").also {
        if (!databasePath.exists()) {
            HistoryDatabase.Schema.create(it)
        }
    }
}
