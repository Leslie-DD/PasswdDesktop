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
                requestTimeoutMillis = 3000
            }
            install(Logging) {
                level = LogLevel.NONE
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
    }


    @Throws
    fun forceUpdateHttpClient(host: String, port: Int) {
        _httpClient = createHttpClient(host, port)
    }

    private fun isIpv4HostValid(hostStr: String): Boolean {
        return IPV4_PATTERN.matcher(hostStr).matches()
    }

}