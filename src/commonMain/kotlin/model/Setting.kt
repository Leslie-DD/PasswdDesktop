package model

import config.LocalPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Setting {

    val screenOrientation = MutableStateFlow(LocalPref.screenOrientation)

    companion object {

    }

    init {
        CoroutineScope(Dispatchers.IO).apply {


            launch {
                screenOrientation.collectLatest {
                    LocalPref.screenOrientation = it
                }
            }

        }
    }
}