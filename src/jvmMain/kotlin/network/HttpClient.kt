package network

import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val httpClient = httpClient {
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTP     // HTTP协议
            host = ""                       // 本地IP
            port = 19999                    // 本地端口
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 3000
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}