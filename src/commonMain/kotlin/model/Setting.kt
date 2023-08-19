package model

import config.LocalPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Setting {

    val screenOrientation = MutableStateFlow(LocalPref.screenOrientation)

    val accessToken = MutableStateFlow(LocalPref.accessToken)

    val secretKey = MutableStateFlow(LocalPref.secretKey)

    companion object {

    }

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

        }
    }
}