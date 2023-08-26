package model

import config.LocalPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object Setting {

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