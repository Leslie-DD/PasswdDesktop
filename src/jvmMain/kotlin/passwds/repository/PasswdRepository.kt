package passwds.repository

import config.LocalPref
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.datasource.RemoteDataSource
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult
import utils.AESUtil
import java.util.*

class PasswdRepository(
    private val remoteDataSource: RemoteDataSource = RemoteDataSource()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val passwds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())

    val groups = MutableStateFlow<MutableList<Group>>(arrayListOf())

    val groupPasswds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())


    suspend fun fetchPasswds() {
        remoteDataSource.fetchPasswds()
            .onSuccess {
                passwds.emit(it)
            }.onFailure {
                logger.error("$TAG fetchPasswds error, ${it.message}")
                passwds.emit(arrayListOf())
                it.printStackTrace()
            }
    }

    suspend fun fetchGroups() {
        remoteDataSource.fetchGroups()
            .onSuccess {
                groups.emit(it)
            }.onFailure {
                logger.error("$TAG fetchGroups error, ${it.message}")
                groups.emit(arrayListOf())
                it.printStackTrace()
            }
    }

    suspend fun fetchGroupPasswds(
        groupId: Int
    ) {
        remoteDataSource.fetchGroupPasswds(
            groupId
        ).onSuccess {
            val secretKeyBytes = Base64.getDecoder().decode(LocalPref.secretKey)
            it.forEach { passwd ->
                decodePasswd(passwd, secretKeyBytes)
            }
            groupPasswds.emit(it)
        }.onFailure {
            logger.error("$TAG fetchGroupPasswds error, ${it.message}")
            groupPasswds.emit(arrayListOf())
            it.printStackTrace()
        }
    }


    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> {
        return remoteDataSource.loginByToken(
            username = username,
            token = token,
            secretKey = secretKey
        ).onSuccess { loginResult ->
            passwds.emit(loginResult.passwds)
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> {
        return remoteDataSource.loginByPassword(
            username = username,
            password = password,
            secretKey = secretKey
        ).onSuccess { loginResult ->
            passwds.emit(loginResult.passwds)
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult?> {
        return remoteDataSource.register(
            username = username,
            password = password
        ).onSuccess { loginResult ->
            passwds.emit(arrayListOf())
            groups.emit(arrayListOf())
            groupPasswds.emit(arrayListOf())
            Result.success(loginResult)
        }.onFailure {
            Result.failure<LoginResult>(it)
        }
    }

    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Int> = remoteDataSource.newGroup(
        groupName = groupName,
        groupComment = groupComment
    )

    suspend fun deleteGroup(
        groupId: Int
    ): Result<Int> = remoteDataSource.deleteGroup(
        groupId = groupId
    )

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Result<Int> = remoteDataSource.updateGroup(
        groupId = groupId,
        groupName = groupName,
        groupComment = groupComment
    )

    suspend fun newPasswd(
        groupId: Int,
        title: String,
        usernameString: String,
        passwordString: String,
        link: String,
        comment: String,
    ): Result<Int> {
        val secretKey = LocalPref.secretKey
        return remoteDataSource.newPasswd(
            groupId = groupId,
            title = title,
            usernameString = encode(secretKey, usernameString),
            passwordString = encode(secretKey, passwordString),
            link = link,
            comment = comment
        )
    }

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameStr: String?,
        passwordStr: String?,
        link: String?,
        comment: String?
    ): Result<Int> {
        val secretKey = LocalPref.secretKey
        return remoteDataSource.updatePasswd(
            id = id,
            title = title,
            usernameStr = encode(secretKey, usernameStr ?: ""),
            passwordStr = encode(secretKey, passwordStr ?: ""),
            link = link,
            comment = comment
        )
    }

    suspend fun deletePasswd(
        id: Int
    ): Result<Int> = remoteDataSource.deletePasswd(
        id = id
    )

    private fun decodePasswd(passwd: Passwd, secretKeyBytes: ByteArray?): Passwd {
        logger.debug("fetchGroupPasswd decode before: passwd: {}", passwd)
        try {
            passwd.usernameString?.let {
                passwd.usernameString = decode(it, secretKeyBytes)
            }
            passwd.passwordString?.let {
                passwd.passwordString = decode(it, secretKeyBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logger.debug("fetchGroupPasswd decode after: passwd: {}", passwd)
        return passwd
    }

    private fun decode(value: String, secretKeyBytes: ByteArray?): String {
        return if (value.isBlank()) {
            value
        } else {
            val decodePasswordResult =
                AESUtil.decrypt(secretKeyBytes, Base64.getDecoder().decode(value))
            if (decodePasswordResult != null) {
                String(decodePasswordResult)
            } else {
                value
            }
        }
    }

    private fun encode(secretKey: String, value: String): String {
        return if (value.isBlank()) {
            value
        } else {
            Base64.getEncoder().encodeToString(AESUtil.encrypt(secretKey, value))
        }
    }

    companion object {
        private const val TAG = "PasswdRepository"
    }
}