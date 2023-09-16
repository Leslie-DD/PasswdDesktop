import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggerContextListener
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.LifeCycle
import utils.FileUtils

open class LoggerStartupListener : ContextAwareBase(), LoggerContextListener, LifeCycle {

    private var started = false

    override fun start() {
        val logFile = FileUtils.getFileInUserHome("passwd.log")
        getContext().putProperty("logFilePath", logFile.absolutePath)
        println("[LoggerStartupListener] (start) logFilePath: " + logFile.absolutePath)
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