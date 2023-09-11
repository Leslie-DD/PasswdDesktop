package network

import database.DataBase
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import network.entity.KtorResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(DelicateCoroutinesApi::class)
object KtorRequest {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    var accessToken: String = ""
    var userId: Int = -1

    init {
        GlobalScope.launch {
            launch {
                DataBase.instance.globalAccessToken.collectLatest {
                    accessToken = it
                }
            }
            launch {
                DataBase.instance.globalUserId.collectLatest {
                    userId = it
                }
            }
        }
    }

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T> postRequest(
        needToken: Boolean = true,
        needUserId: Boolean = true,
        api: String,
        params: List<Param<Any>>? = null
    ): Result<T> = runCatching {
        logger.info("postRequest ($api) params: $params, accessToken: $accessToken, userId: $userId")
        return httpClient.post {
            url(api)
            setBody(MultiPartFormDataContent(formData {
                if (needToken) {
                    headers {
                        append("access_token", accessToken)
                    }
                }
                if (needUserId) {
                    append("user_id", userId)
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