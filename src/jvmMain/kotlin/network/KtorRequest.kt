package network

import config.LocalPref
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import network.Api.API_DELETE_GROUP
import network.Api.API_GROUPS
import network.Api.API_GROUP_PASSWDS
import network.Api.API_LOGIN_BY_PASSWORD
import network.Api.API_LOGIN_BY_TOKEN
import network.Api.API_NEW_GROUP
import network.Api.API_PASSWDS
import network.Api.API_REGISTER
import network.entity.KtorResult
import passwds.entity.Group
import passwds.entity.LoginResult
import passwds.entity.Passwd
import passwds.entity.RegisterResult

object KtorRequest {

    suspend fun postPasswds(): Result<List<Passwd>> = runCatching {
        return httpClient.post {
            url(API_PASSWDS)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", LocalPref.accessToken)
                }
                append("user_id", LocalPref.userId)
                append("secret_key", LocalPref.secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<List<Passwd>>>().result()
    }

    suspend fun postGroups(): Result<MutableList<Group>> = runCatching {
        return httpClient.post {
            url(API_GROUPS)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", LocalPref.accessToken)
                }
                append("user_id", LocalPref.userId)
                append("secret_key", LocalPref.secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<MutableList<Group>>>().result()
    }

    suspend fun postGroupPasswds(groupId: Int): Result<List<Passwd>> = runCatching {
        return httpClient.post {
            url(API_GROUP_PASSWDS)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", LocalPref.accessToken)
                }
                append("user_id", LocalPref.userId)
                append("group_id", groupId)
                append("secret_key", LocalPref.secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<List<Passwd>>>().result()
    }

    suspend fun loginByToken(
        username: String,
        token: String,
        secretKey: String
    ): Result<LoginResult> = runCatching {
        return httpClient.post {
            url(API_LOGIN_BY_TOKEN)
            setBody(MultiPartFormDataContent(formData {
                append("username", username)
                append("token", token)
                append("secret_key", secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<LoginResult>>().result()
    }

    suspend fun loginByPassword(
        username: String,
        password: String,
        secretKey: String
    ): Result<LoginResult> = runCatching {
        return httpClient.post {
            url(API_LOGIN_BY_PASSWORD)
            setBody(MultiPartFormDataContent(formData {
                append("username", username)
                append("password", password)
                append("secret_key", secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<LoginResult>>().result()
    }

    suspend fun register(
        username: String,
        password: String,
    ): Result<RegisterResult> = runCatching {
        return httpClient.post {
            url(API_REGISTER)
            setBody(MultiPartFormDataContent(formData {
                append("username", username)
                append("password", password)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<RegisterResult>>().result()
    }

    /**
     * @return the group id added
     */
    suspend fun newGroup(
        groupName: String,
        groupComment: String,
    ): Result<Int> = runCatching {
        return httpClient.post {
            url(API_NEW_GROUP)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", LocalPref.accessToken)
                }
                append("user_id", LocalPref.userId)
                append("group_name", groupName)
                append("group_comment", groupComment)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<Int>>().result()
    }

    /**
     * @return the group count that deleted, always 1 if deleted successfully
     */
    suspend fun deleteGroup(
        groupId: Int,
    ): Result<Int> = runCatching {
        return httpClient.post {
            url(API_DELETE_GROUP)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", LocalPref.accessToken)
                }
                append("user_id", LocalPref.userId)
                append("group_id", groupId)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<Int>>().result()
    }

}