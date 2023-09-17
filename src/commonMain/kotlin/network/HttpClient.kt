package network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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

fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(CIO) {
    config(this)
}