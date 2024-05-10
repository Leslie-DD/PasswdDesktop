package network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

object HttpClientObj {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private const val IPV4_REGEX = """(((\d{1,2})|(1\d{2})|(2[0-4]\d)|(25[0-5]))\.){3}((\d{1,2})|(1\d{2})|(2[0-4]\d)|(25[0-5]))"""
    private val IPV4_PATTERN: Pattern = Pattern.compile(IPV4_REGEX)

    @get:Synchronized
    private var _httpClient: HttpClient? = null

    val httpClient: HttpClient?
        get() = _httpClient

    private var webSocketSession: DefaultClientWebSocketSession? = null

    @Throws
    private fun createHttpClient(hostStr: String, portInt: Int): HttpClient {
        if (!isIpv4HostValid(hostStr)) {
            throw Throwable("ipv4 $hostStr invalid")
        }
        return HttpClient(CIO) {
            logger.info("createHttpClient, host: $hostStr, port: $portInt")
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTP
                    host = hostStr
                    port = portInt
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(Logging) {
                level = LogLevel.ALL
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
            install(WebSockets)
        }
    }

    suspend fun startWebSocketListener(host: String, port: Int, userId: Int) {
        httpClient?.ws(
            method = HttpMethod.Get,
            host = host,
            port = port,
            path = "/Passwd/webSocket/$userId"
        ) {
            logger.info("Create a new webSocketSession $this")
            webSocketSession = this
            incoming.receiveAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect {
                    // process frame
                    logger.info("websocket incoming.receive() ${it.readText()}")
                }
        }
    }

    @Throws
    suspend fun forceUpdateHttpClient(host: String, port: Int) {
        closeSocketSession()
        _httpClient = createHttpClient(host, port)
    }

    private suspend fun closeSocketSession(closeReason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, "")) {
        logger.info("Close an old webSocketSession $webSocketSession")
        webSocketSession?.close(closeReason)
        webSocketSession = null
    }

    private fun isIpv4HostValid(hostStr: String): Boolean {
        return IPV4_PATTERN.matcher(hostStr).matches()
    }

}