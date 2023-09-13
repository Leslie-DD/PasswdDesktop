package network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun getPlatformName(): String
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient