import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.passwd.common.database.HistoryDatabase
import network.KtorRequest.logger
import java.io.File

actual fun createDriver(): SqlDriver {
    val databasePath = File(System.getProperty("compose.application.resources.dir"), "HistoryDatabase.db")
    logger.info("HistoryDatabase.db absolute path: ${databasePath.absolutePath}")
    return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}").also {
        if (!databasePath.exists()) {
            HistoryDatabase.Schema.create(it)
        }
    }
}
