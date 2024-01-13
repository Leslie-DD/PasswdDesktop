package datasource

import database.DataBase
import database.entity.HistoryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DatabaseDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val database = DataBase.instance

    private val _historyDataFlow = MutableStateFlow<HistoryData?>(null)
    val historyData = _historyDataFlow.asStateFlow()

    val savedHistories
        get() = database.getSavedHistories()

    val latestSavedLoginHistoryData
        get() = database.latestSavedLoginHistoryData()

    fun insertHistoryData(
        username: String,
        password: String,
        secretKey: String,
        host: String,
        port: Int,
        accessToken: String,
        saved: Boolean,
        silentlyLogin: Boolean
    ) {
        logger.debug("(insertHistoryData). username: $username, password: $password, secretKey: $secretKey, saved: $saved")
        val insertHistoryData = HistoryData(
            username = username,
            password = if (saved) password else "",
            secretKey = secretKey,
            host = host,
            port = port,
            accessToken = accessToken,
            saved = saved,
            silentlyLogin = silentlyLogin
        )
        val insertResultId = database.insert(insertHistoryData)
        logger.debug("(insertHistoryData) insertResultId: $insertResultId")

        _historyDataFlow.value = if (insertResultId == -1) {
            HistoryData.defaultHistoryData()
        } else {
            insertHistoryData
        }
    }
}