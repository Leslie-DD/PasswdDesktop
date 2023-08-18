package passwds.repository

import network.KtorRequest
import passwds.entity.Group
import passwds.entity.Passwd

class PasswdRepository {
    suspend fun fetchPasswds(): Result<List<Passwd>> = KtorRequest.postPasswds()
    suspend fun fetchGroups(): Result<List<Group>> = KtorRequest.postGroups()
    suspend fun fetchGroupPasswds(groupId: Int): Result<List<Passwd>> = KtorRequest.postGroupPasswds(groupId)
}