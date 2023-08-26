package network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import model.Setting
import network.entity.KtorResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KtorRequest {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T> postRequest(
        needToken: Boolean = true,
        needUserId: Boolean = true,
        needSecretKey: Boolean = true,
        api: String,
        params: List<Param<Any>>? = null
    ): Result<T> = runCatching {
        logger.info("postRequest ($api)")
        return httpClient.post {
            url(api)
            setBody(MultiPartFormDataContent(formData {
                if (needToken) {
                    headers {
                        append("access_token", Setting.accessToken.value)
                    }
                }
                if (needUserId) {
                    append("user_id", Setting.userId.value)
                }
                if (needSecretKey) {
                    append("secret_key", Setting.secretKey.value)
                }
                params?.let {
                    it.forEach { param ->
                        append(param.name, param.value)
                    }
                }
            }))
            contentType(ContentType.Application.Json)
        }.body<KtorResult<T>>()
            .apply { this.api = api }
            .result()
    }

}