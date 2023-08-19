package network

import config.LocalPref
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import network.Api.API_GROUPS
import network.Api.API_GROUP_PASSWDS
import network.Api.API_PASSWDS
import network.entity.KtorResult
import passwds.entity.Group
import passwds.entity.Passwd

object KtorRequest {

    const val ACCESS_TOKEN =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE2OTI5NDg2NTgsInVzZXJuYW1lIjoibHVjYXMifQ.EZADpCZW4CmOyx8O30r1i2gVHKkzsQSLcueHRgg_aFE"
    const val SECRET_KEY = "SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ="

    suspend fun postPasswds(): Result<List<Passwd>> = runCatching {
        return httpClient.post {
            url(API_PASSWDS)
            setBody(MultiPartFormDataContent(partData()))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<List<Passwd>>>().result()
    }

    suspend fun postGroups(): Result<List<Group>> = runCatching {
        return httpClient.post {
            url(API_GROUPS)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", ACCESS_TOKEN)
                }
                append("user_id", 1)
                append("secret_key", LocalPref.secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<List<Group>>>().result()
    }

    suspend fun postGroupPasswds(groupId: Int): Result<List<Passwd>> = runCatching {
        return httpClient.post {
            url(API_GROUP_PASSWDS)
            setBody(MultiPartFormDataContent(formData {
                headers {
                    append("access_token", ACCESS_TOKEN)
                }
                append("user_id", 1)
                append("group_id", groupId)
                append("secret_key", LocalPref.secretKey)
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<List<Passwd>>>().result()
    }

    private fun HttpRequestBuilder.partData() = formData {
        headers {
            append("access_token", ACCESS_TOKEN)
        }
        append("user_id", 1)
        append("secret_key", LocalPref.secretKey)
    }

}