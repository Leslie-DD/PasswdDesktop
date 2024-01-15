package datasource

import database.user.DataBase
import database.entity.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DatabaseDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val database = DataBase.instance

    private val _userDataFlow = MutableStateFlow<UserData?>(null)
    val userData = _userDataFlow.asStateFlow()

    val savedUsers
        get() = database.getSavedUsers()

    val latestSavedUserData
        get() = database.latestSavedUserData()

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
        val insertUserData = UserData(
            username = username,
            password = if (saved) password else "",
            secretKey = secretKey,
            host = host,
            port = port,
            accessToken = accessToken,
            saved = saved,
            silentlyLogin = silentlyLogin
        )
        val insertResultId = database.insert(insertUserData)
        logger.debug("(insertHistoryData) insertResultId: $insertResultId")

        _userDataFlow.value = if (insertResultId == -1) {
            UserData.defaultUserData()
        } else {
            insertUserData
        }
    }
}