package passwds.repository

import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import passwds.datasource.RemoteDataSource
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult

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
                it.printStackTrace()
            }
    }

    suspend fun fetchGroups() {
        remoteDataSource.fetchGroups()
            .onSuccess {
                groups.emit(it)
            }.onFailure {
                logger.error("$TAG fetchGroups error, ${it.message}")
                it.printStackTrace()
            }
    }

    suspend fun fetchGroupPasswds(
        groupId: Int
    ) {
        remoteDataSource.fetchGroupPasswds(
            groupId
        ).onSuccess {
            groupPasswds.emit(it)
        }.onFailure {
            logger.error("$TAG fetchGroupPasswds error, ${it.message}")
            it.printStackTrace()
        }
    }


    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> = remoteDataSource.loginByToken(
        username = username,
        token = token,
        secretKey = secretKey
    )

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> = remoteDataSource.loginByPassword(
        username = username,
        password = password,
        secretKey = secretKey
    )

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult> = remoteDataSource.register(
        username = username,
        password = password
    )

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
        username: String,
        password: String,
        link: String,
        comment: String,
    ): Result<Int> = remoteDataSource.newPasswd(
        groupId = groupId,
        title = title,
        username = username,
        password = password,
        link = link,
        comment = comment
    )

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameStr: String?,
        passwordStr: String?,
        link: String?,
        comment: String?
    ): Result<Int> = remoteDataSource.updatePasswd(
        id = id,
        title = title,
        usernameStr = usernameStr,
        passwordStr = passwordStr,
        link = link,
        comment = comment
    )

    suspend fun deletePasswd(
        id: Int
    ): Result<Int> = remoteDataSource.deletePasswd(
        id = id
    )

    companion object {
        private const val TAG = "PasswdRepository"
    }
}