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
            userIdAdapter = longOfIntAdapter
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
            dbQuery.insertHistory(
                History(
                    id,
                    userId,
                    username,
                    password,
                    secretKey,
                    accessToken,
                    saved,
                    silentlySignIn,
                    createTime
                )
            )
        }
        return (getHistoryByUserId(userId = item.userId)?.id ?: -1)

    }

    private fun getHistoryByUserId(userId: Int): HistoryData? {
        dbQuery.getHistoryByUserId(userId = userId, ::mapHistoryList).executeAsList().also {
            return if (it.isEmpty()) null else it[0]
        }
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

    private fun mapHistoryList(
        id: Int,
        userId: Int?,
        username: String?,
        password: String?,
        secretKey: String?,
        accessToken: String?,
        saved: Boolean?,
        silentlySignIn: Boolean?,
        createTime: Long?,
    ): HistoryData {
        return HistoryData(
            id = id,
            userId = userId ?: 0,
            username = username ?: "",
            password = password ?: "",
            secretKey = secretKey ?: "",
            accessToken = accessToken ?: "",
            saved = saved ?: false,
            silentlySignIn = silentlySignIn ?: false,
            createTime = createTime ?: 0
        )
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DataBase()
        }
    }

}