package platform.desktop

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.passwd.common.database.ApplicationDatabase
import network.KtorRequest
import platform.Platform
import utils.FileUtils
import java.io.File

actual val currentPlatform: Platform = Platform.Desktop

actual val storageDir: File = File(System.getProperty("user.home"), "Passwd/")

const val DB_FILENAME = "Database.db"

actual fun createSqlDriver(): SqlDriver {
    val databasePath = FileUtils.getFileInUserHome(DB_FILENAME)
    KtorRequest.logger.info("$DB_FILENAME absolute path: ${databasePath.absolutePath}")
    return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}").also {
        ApplicationDatabase.Schema.create(it)
        KtorRequest.logger.info("$DB_FILENAME create success, version ${ApplicationDatabase.Schema.version}")
    }
}
