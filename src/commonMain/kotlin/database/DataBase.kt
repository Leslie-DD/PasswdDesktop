package database

import app.cash.sqldelight.ColumnAdapter
import com.passwd.common.database.History
import com.passwd.common.database.HistoryDatabase
import database.entity.HistoryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import platform.desktop.createSqlDriver

internal class DataBase {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val globalUserId = MutableStateFlow(-1)
    val globalUsername = MutableStateFlow("")
    val globalSecretKey = MutableStateFlow("")
    val globalAccessToken = MutableStateFlow("")

    private val longOfIntAdapter = object : ColumnAdapter<Int, Long> {
        override fun decode(databaseValue: Long): Int {
            return databaseValue.toInt()
        }

        override fun encode(value: Int): Long {
            return value.toLong()
        }
    }

    private val database = HistoryDatabase(
        createSqlDriver(),
        HistoryAdapter = History.Adapter(
            idAdapter = longOfIntAdapter,
            userIdAdapter = longOfIntAdapter,
            portAdapter = longOfIntAdapter
        )
    )

    private val dbQuery = database.historyDatabaseQueries

    internal fun delete(historyData: HistoryData?) {
        if (historyData == null) {
            dbQuery.deleteAllHistory()
        } else {
            dbQuery.deleteHistoryById(historyData.id)
        }
    }

    internal fun getAll(): List<HistoryData> {
        return dbQuery.getAllHistory(::mapHistoryList).executeAsList()
    }

    internal fun insert(item: HistoryData): Int {
        logger.debug("DataBase insert result")
        item.run {
            dbQuery.deleteHistoryByUsername(item.username)
            dbQuery.insertHistory(mapToHistory())
        }
        return (getHistoryByUserId(userId = item.userId)?.id ?: -1)
    }

    private fun getHistoryByUserId(userId: Int): HistoryData? {
        dbQuery.getHistoryByUserId(userId = userId, ::mapHistoryList).executeAsList().also {
            return if (it.isEmpty()) null else it[0]
        }
    }

    fun getSavedHistories(): List<HistoryData> {
        return dbQuery.getSavedHistories(::mapHistoryList).executeAsList()
    }

    /**
     * 上次登录的并且saved的用户
     */
    internal fun latestSavedLoginHistoryData(): HistoryData? {
        dbQuery.latestLoginHistory(::mapHistoryList).executeAsList().also {
            return if (it.isEmpty()) null else {
                val result = it[0]
                if (result.saved) result else null
            }
        }
    }

    internal fun updateHistoryAccessTokenById(id: Int, accessToken: String) {
        dbQuery.updateHistoryAccessTokenById(id = id, accessToken = accessToken)
    }

    internal fun getAccessTokenByUserId(userId: Int): String {
        dbQuery.getAccessTokenByUserId(userId = userId).executeAsList().also {
            return if (it.isEmpty()) "" else it[0].toString()
        }
    }

    internal fun updateHistoryUserIdById(userId: Int, id: Int) {
        dbQuery.updateHistoryUserIdById(userId = userId, id = id)
    }

    internal fun updateHistoryUpdateTimeById(id: Int) {
        dbQuery.updateHistoryUpdateTimeById(updateTime = Clock.System.now().epochSeconds, id = id)
    }


    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DataBase()
        }
    }

}