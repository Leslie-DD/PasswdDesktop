package datasource.passwd

import entity.Group
import entity.Passwd
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 此 DataSource 作为单一数据源
 */
object PasswdMemoryDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    var passwdsMap: MutableMap<Int, MutableList<Passwd>> = mutableMapOf()
    val groups = MutableStateFlow<MutableList<Group>>(arrayListOf())
    val groupPasswds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())

    private fun MutableList<Passwd>.mapToPasswdsMap(): MutableMap<Int, MutableList<Passwd>> {
        val passwdsMapResult: MutableMap<Int, MutableList<Passwd>> = hashMapOf()
        forEach { passwd ->
            if (passwdsMapResult[passwd.groupId] == null) {
                passwdsMapResult[passwd.groupId] = arrayListOf()
            }
            passwdsMapResult[passwd.groupId]?.add(passwd)
        }
        return passwdsMapResult
    }

    suspend fun onLoginSuccess(passwds: MutableList<Passwd>): List<Passwd> {
        passwdsMap = passwds.mapToPasswdsMap()
        emitGroups(arrayListOf())
        emitGroupPasswds(arrayListOf())
        return passwdsMap.toList().flatMap { it.second }
    }

    suspend fun onSignupSuccess() {
        passwdsMap = mutableMapOf()
        emitGroups(arrayListOf())
        emitGroupPasswds(arrayListOf())
    }

    suspend fun newGroup(newGroup: Group) {
        passwdsMap[newGroup.id] = mutableListOf()
//        val groupList = groups.value.toMutableList().apply {
//            add(newGroup)
//        }
//        emitGroups(groupList)
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
        updateGroup: Group
    ) {
        val originGroups = groups.value
        originGroups.find { group: Group -> group.id == updateGroup.id }?.let {
            it.groupName = updateGroup.groupName
            it.groupComment = updateGroup.groupComment
        }
        emitGroups(originGroups)
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
        updatePasswd: Passwd
    ): Passwd? {
        var updateResultPasswd: Passwd? = null
        passwdsMap.flatMap { it.value }.find { it.id == updatePasswd.id }?.let { originPasswd ->
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