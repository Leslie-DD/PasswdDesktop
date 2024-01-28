package database.user

import database.mapToGroup
import database.mapToPasswd
import database.mapToTableGroup
import database.mapToTablePasswd
import datasource.user.UserMemoryDataSource
import entity.Group
import entity.Passwd
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PasswdDataBaDataSource {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val groupQuery
        get() = DataBase.instance.groupQuery

    private val passwdQuery
        get() = DataBase.instance.passwdQuery

    // --------------------------------Group---------------------------------//
    private fun getGroup(groupId: Int): Group? = groupQuery.getGroupById(id = groupId, ::mapToGroup).executeAsOneOrNull()

    fun updateGroups(groups: List<Group>) {
        groups.forEach { updateOrInsertGroupIfNotExist(it) }
    }

    fun updateOrInsertGroupIfNotExist(group: Group): Group {
        return updateOrInsertGroupIfNotExist(
            groupId = group.id,
            groupName = group.groupName ?: "",
            groupComment = group.groupComment ?: ""
        )
    }
    fun updateOrInsertGroupIfNotExist(groupId: Int, groupName: String, groupComment: String): Group {
        val updateGroup: Group
        val group = getGroup(groupId)
        if (group == null) {
            updateGroup = Group(
                id = groupId,
                userId = UserMemoryDataSource.globalUserId.value,
                groupName = groupName,
                groupComment = groupComment
            )
            groupQuery.insertGroup(updateGroup.mapToTableGroup())
        } else {
            updateGroup = group.copy(
                groupName = groupName,
                groupComment = groupComment
            )
            groupQuery.deleteGroup(groupId)
            groupQuery.insertGroup(updateGroup.mapToTableGroup())
        }
        return updateGroup
    }

    fun deleteGroup(groupId: Int) {
        groupQuery.deleteGroup(groupId)
        passwdQuery.deletePasswdByGroupId(groupId)
    }


    // --------------------------------Passwd--------------------------------//
    private fun getPasswd(passwdId: Int): Passwd? {
        return passwdQuery.getPasswdById(id = passwdId, ::mapToPasswd).executeAsOneOrNull()
    }

    fun updatePasswds(passwds: List<Passwd>) {
        passwds.forEach { updateOrInsertPasswdIfNotExist(it) }
    }

    fun updateOrInsertPasswdIfNotExist(passwd: Passwd) {
        updateOrInsertPasswdIfNotExist(
            id = passwd.id,
            groupId = passwd.groupId,
            title = passwd.title,
            usernameString = passwd.usernameString,
            passwordString = passwd.passwordString,
            link = passwd.link,
            comment = passwd.comment
        )
    }

    fun updateOrInsertPasswdIfNotExist(
        id: Int,
        groupId: Int,
        title: String?,
        usernameString: String?,
        passwordString: String?,
        link: String?,
        comment: String?
    ): Passwd {
        val updatePasswd: Passwd
        val dbPasswd = getPasswd(id)
        if (dbPasswd == null) {
            // TODO: 正常情况下不应该走到这里
            logger.warn("dbPasswd($id) is null")
            updatePasswd = Passwd(
                id = id,
                groupId = groupId,
                title = title,
                userId = UserMemoryDataSource.globalUserId.value,
                link = link,
                comment = comment,
                usernameString = usernameString,
                passwordString = passwordString
            )
            passwdQuery.insertPasswd(updatePasswd.mapToTablePasswd())
        } else {
            updatePasswd = dbPasswd.copy(
                title = title,
                groupId = groupId,
                usernameString = usernameString,
                passwordString = passwordString,
                link = link,
                comment = comment
            )
            passwdQuery.deletePasswd(updatePasswd.id)
            passwdQuery.insertPasswd(updatePasswd.mapToTablePasswd())
        }
        return updatePasswd
    }

    fun deletePasswd(passwdId: Int) {
        passwdQuery.deletePasswd(passwdId)
    }

}