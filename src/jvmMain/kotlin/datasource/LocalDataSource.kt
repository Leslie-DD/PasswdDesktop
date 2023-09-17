package datasource

import entity.Group
import entity.Passwd
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 此 DataSource 作为单一数据源
 */
class LocalDataSource {

    var passwdsMap: MutableMap<Int, MutableList<Passwd>> = mutableMapOf()
    val groups = MutableStateFlow<MutableList<Group>>(arrayListOf())
    val groupPasswds = MutableStateFlow<MutableList<Passwd>>(arrayListOf())

    suspend fun updateGroups(
        groupList: MutableList<Group>
    ) {
        groups.emit(groupList)
    }

    suspend fun updateGroupPasswds(
        groupId: Int,
        convert: (MutableList<Passwd>) -> MutableList<Passwd> = { passwds -> passwds }
    ): MutableList<Passwd> {
        return (passwdsMap[groupId] ?: arrayListOf()).also {
            groupPasswds.emit(convert(it))
        }
    }


    suspend fun updateGroupPasswds(passwds: MutableList<Passwd>) {
        groupPasswds.emit(passwds)
    }

}