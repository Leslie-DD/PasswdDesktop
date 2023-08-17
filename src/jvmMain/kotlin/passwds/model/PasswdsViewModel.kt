package passwds.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import network.KtorRequest
import org.jetbrains.skia.impl.Log
import passwds.entity.Passwd

class PasswdsViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val _passwds = MutableStateFlow<List<Passwd>>(emptyList())
    val passwds: StateFlow<List<Passwd>> get() = _passwds

    suspend fun fetchPasswds() = withContext(Dispatchers.IO) {
        Log.debug("PasswdsViewModel().fetchPasswds start")
        KtorRequest.postPasswds()
            .onSuccess {
                Log.info("PasswdsViewModel().fetchPasswds success, size:${it.size}")
                _passwds.value = it
            }.onFailure {
                Log.error("PasswdsViewModel().fetchPasswds error:${it.message}")
                it.printStackTrace()
            }
    }


}