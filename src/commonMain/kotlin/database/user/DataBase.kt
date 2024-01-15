package database.user

import app.cash.sqldelight.ColumnAdapter
import com.passwd.common.database.ApplicationDatabase
import com.passwd.common.database.Passwd_user
import database.entity.UserData
import database.mapToUserData
import database.mapToPasswdUser
import kotlinx.datetime.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import platform.desktop.createSqlDriver

internal class DataBase {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val longOfIntAdapter = object : ColumnAdapter<Int, Long> {
        override fun decode(databaseValue: Long): Int = databaseValue.toInt()
        override fun encode(value: Int): Long = value.toLong()
    }

    private val database by lazy {
        ApplicationDatabase(
            createSqlDriver(),
            passwd_userAdapter = Passwd_user.Adapter(
                idAdapter = longOfIntAdapter,
                userIdAdapter = longOfIntAdapter,
                portAdapter = longOfIntAdapter
            )
        )
    }

    private val userQuery = database.passwd_userQueries

    internal fun delete(userData: UserData?) {
        if (userData == null) {
            userQuery.deleteAllUsers()
        } else {
            userQuery.deleteUserById(userData.id)
        }
    }

    internal fun getAll(): List<UserData> {
        return userQuery.getAllUsers(::mapToUserData).executeAsList()
    }

    internal fun insert(item: UserData): Int {
        logger.debug("DataBase insert user: ${item.username}")
        item.run {
            userQuery.deleteUserByUsername(item.username)
            userQuery.insertUser(mapToPasswdUser())
        }
        return (getUserByUserId(userId = item.userId)?.id ?: -1)
    }

    private fun getUserByUserId(userId: Int): UserData? {
        userQuery.getUserByUserId(userId = userId, ::mapToUserData).executeAsList().also {
            return if (it.isEmpty()) null else it[0]
        }
    }

    fun getSavedUsers(): List<UserData> {
        return userQuery.getSavedUsers(::mapToUserData).executeAsList()
    }

    /**
     * 上次登录的并且saved的用户
     */
    internal fun latestSavedUserData(): UserData? {
        userQuery.latestLoginUser(::mapToUserData).executeAsList().also {
            return if (it.isEmpty()) null else {
                val result = it[0]
                if (result.saved) result else null
            }
        }
    }

    internal fun updateUserAccessTokenById(id: Int, accessToken: String) {
        userQuery.updateUserAccessTokenById(id = id, accessToken = accessToken)
    }

    internal fun getAccessTokenByUserId(userId: Int): String {
        userQuery.getAccessTokenByUserId(userId = userId).executeAsList().also {
            return if (it.isEmpty()) "" else it[0].toString()
        }
    }

    internal fun updateUserIdById(userId: Int, id: Int) {
        userQuery.updateUserIdById(userId = userId, id = id)
    }

    internal fun updateUserUpdateTimeById(id: Int) {
        userQuery.updateUserUpdateTimeById(updateTime = Clock.System.now().epochSeconds, id = id)
    }


    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DataBase()
        }
    }

}