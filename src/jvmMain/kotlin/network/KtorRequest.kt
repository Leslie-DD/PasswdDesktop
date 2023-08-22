package network

import config.LocalPref
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import network.entity.KtorResult

object KtorRequest {

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T> postRequest(
        needToken: Boolean = true,
        needUserId: Boolean = true,
        needSecretKey: Boolean = true,
        api: String,
        params: List<Param<Any>>? = null
    ): Result<T> = runCatching {
        return httpClient.post {
            url(api)
            setBody(MultiPartFormDataContent(formData {
                if (needToken) {
                    headers {
                        append("access_token", LocalPref.accessToken)
                    }
                }
                if (needUserId) {
                    append("user_id", LocalPref.userId)
                }
                if (needSecretKey) {
                    append("secret_key", LocalPref.secretKey)
                }
                params?.let {
                    it.forEach { param ->
                        append(param.name, param.value)
                    }
                }
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<T>>().result()
    }

}