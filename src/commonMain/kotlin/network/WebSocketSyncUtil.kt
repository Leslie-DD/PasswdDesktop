package network

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WebSocketSyncUtil {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @OptIn(DelicateCoroutinesApi::class)
    fun startWebSocketListener(host: String, port: Int, userId: Int) {
        GlobalScope.launch(Dispatchers.Default) {
            HttpClientObj.httpClient?.ws(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/Passwd/webSocket/$userId"
            ) {
//                logger.info("send websocket")
//                send(Frame.Text("Hello World"))
                incoming.receiveAsFlow()
                    .filterIsInstance<Frame.Text>()
                    .collect {
                        // process frame
                        logger.info("websocket incoming.receive() ${it.readText()}")
                    }
            }
        }
    }
}