package passwds.datasource

import passwds.entity.Group
import passwds.entity.Passwd

class LocalDataSource {

    var passwdsMap: MutableMap<Int, MutableList<Passwd>> = mutableMapOf()
    var groups: MutableList<Group> = mutableListOf()

    fun getGroupPasswds(groupId: Int): MutableList<Passwd> = passwdsMap[groupId] ?: arrayListOf()
}