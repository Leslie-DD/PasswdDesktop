package model

import config.LocalPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

object Setting {
    private val log = LoggerFactory.getLogger(this.javaClass)

    val screenOrientation = MutableStateFlow(LocalPref.screenOrientation)

    val accessToken = MutableStateFlow(LocalPref.accessToken)

    val secretKey = MutableStateFlow(LocalPref.secretKey)

    val userId = MutableStateFlow(LocalPref.userId)

    val username = MutableStateFlow(LocalPref.username)

    init {
        CoroutineScope(Dispatchers.IO).apply {

            launch {
                screenOrientation.collectLatest {
                    LocalPref.screenOrientation = it
                }
            }

            launch {
                accessToken.collectLatest {
                    LocalPref.accessToken = it
                }
            }

            launch {
                secretKey.collectLatest {
                    log.info("secretKey update: $it")
                    LocalPref.secretKey = it
                }
            }

            launch {
                userId.collectLatest {
                    LocalPref.userId = it
                }
            }

            launch {
                username.collectLatest {
                    LocalPref.username = it
                }
            }

        }
    }
}