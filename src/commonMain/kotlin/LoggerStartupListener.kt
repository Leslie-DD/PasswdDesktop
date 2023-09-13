import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggerContextListener
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.LifeCycle
import java.io.File

open class LoggerStartupListener : ContextAwareBase(), LoggerContextListener, LifeCycle {

    private var started = false

    override fun start() {
        val databasePath = File(System.getProperty("compose.application.resources.dir"), "passwd_log.log")
        val context = getContext()
        context.putProperty("logFilePath", databasePath.absolutePath)
        println("[LoggerStartupListener] (start) logFilePath: " + databasePath.absolutePath)
        started = true
    }

    override fun stop() {

    }

    override fun isStarted(): Boolean {
        return started
    }

    override fun isResetResistant(): Boolean {
        return false
    }

    override fun onStart(context: LoggerContext?) {
    }

    override fun onReset(context: LoggerContext?) {

    }

    override fun onStop(context: LoggerContext?) {

    }

    override fun onLevelChange(logger: Logger?, level: Level?) {

    }


}