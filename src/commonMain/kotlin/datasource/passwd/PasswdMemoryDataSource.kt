package datasource.passwd

import entity.Group
import entity.Passwd
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.AESUtil
import java.util.*

/**
 * 此 DataSource 作为单一数据源
 */
object PasswdMemoryDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    var passwdsMap: MutableMap<Int, MutableList<Passwd>> = mutableMapOf()
    val groups = MutableStateFlow<MutableList<Group>>(arrayListOf())
    val groupPasswds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())

    private fun MutableList<Passwd>.mapToPasswdsMap(
        secretKey: String
    ): MutableMap<Int, MutableList<Passwd>> {
        val secretKeyByteArray = Base64.getDecoder().decode(secretKey)
        val passwdsMapResult: MutableMap<Int, MutableList<Passwd>> = hashMapOf()
        forEach { passwd ->
            if (passwdsMapResult[passwd.groupId] == null) {
                passwdsMapResult[passwd.groupId] = arrayListOf()
            }
            passwdsMapResult[passwd.groupId]?.add(decodePasswd(secretKeyByteArray, passwd))
        }
        return passwdsMapResult
    }

    private fun decodePasswd(
        secretKeyBytes: ByteArray? = null,
        passwd: Passwd
    ): Passwd = try {
        passwd.copy(
            title = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.title),
            usernameString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.usernameString),
            passwordString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.passwordString)
        )
    } catch (e: Exception) {
        logger.error("(decodePasswd) error ", e)
        passwd
    }

    suspend fun onLoginSuccess(passwds: MutableList<Passwd>, secretKey: String) {
        passwdsMap = passwds.mapToPasswdsMap(secretKey)
        emitGroups(arrayListOf())
        emitGroupPasswds(arrayListOf())
    }

    suspend fun onSignupSuccess() {
        passwdsMap = mutableMapOf()
        emitGroups(arrayListOf())
        emitGroupPasswds(arrayListOf())
    }

    suspend fun newGroup(newGroup: Group) {
        passwdsMap[newGroup.id] = mutableListOf()
        groups.value.add(newGroup)
        emitGroups(groups.value)
        emitGroupPasswds(arrayListOf())
    }

    suspend fun deleteGroup(groupId: Int): Group? {
        var deleteGroup: Group? = null
        passwdsMap.remove(groupId)
        val originGroups = groups.value
        originGroups.find { group: Group -> group.id == groupId }?.let {
            originGroups.remove(it)
            deleteGroup = it
        }
        emitGroups(originGroups)
        emitGroupPasswds(arrayListOf())
        return deleteGroup
    }

    suspend fun updateGroup(
        groupId: Int,
        groupName: String,
        groupComment: String
    ): Group? {
        var updateGroup: Group? = null
        val originGroups = groups.value
        originGroups.find { group: Group -> group.id == groupId }?.let {
            it.groupName = groupName
            it.groupComment = groupComment
            updateGroup = it
        }
        emitGroups(originGroups)
        return updateGroup
    }

    suspend fun newPasswd(newPasswd: Passwd) {
        val groupPasswds = passwdsMap[newPasswd.groupId]
        if (groupPasswds == null) {
            logger.warn("groupPasswd is null")
            passwdsMap[newPasswd.groupId] = arrayListOf(newPasswd)
        } else {
            groupPasswds.add(newPasswd)
        }
        emitGroupPasswds(newPasswd.groupId)
    }

    suspend fun deletePasswd(passwdId: Int): Passwd? {
        var deletePasswd: Passwd? = null
        passwdsMap.flatMap { it.value }.find { it.id == passwdId }?.let { passwd ->
            if (passwdsMap[passwd.groupId]?.remove(passwd) == true) {
                deletePasswd = passwd
            }
            emitGroupPasswds(passwd.groupId)
        }
        return deletePasswd
    }

    suspend fun updatePasswd(
        id: Int,
        title: String?,
        usernameString: String?,
        passwordString: String?,
        link: String?,
        comment: String?
    ): Passwd? {
        var updateResultPasswd: Passwd? = null
        passwdsMap.flatMap { it.value }.find { it.id == id }?.let { originPasswd ->
            val updatePasswd = originPasswd.copy(
                title = title,
                usernameString = usernameString,
                passwordString = passwordString,
                link = link,
                comment = comment
            )
            passwdsMap[updatePasswd.groupId]?.apply {
                val index = indexOf(originPasswd)
                if (index != -1) {
                    set(index, updatePasswd)
                    updateResultPasswd = updatePasswd
                }
            }
            emitGroupPasswds(updatePasswd.groupId)
        }
        return updateResultPasswd
    }

    suspend fun emitGroupPasswds(groupId: Int) {
        emitGroupPasswds((passwdsMap[groupId] ?: arrayListOf()))
    }

    suspend fun emitGroupPasswds(passwds: MutableList<Passwd>) {
        groupPasswds.emit(passwds)
    }

    suspend fun emitGroups(groupList: MutableList<Group>) {
        groups.emit(groupList)
    }

}