package logger

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
        val logFilesFolder = FileUtils.getFileInUserHome("logFiles")
        if (!logFilesFolder.isDirectory || !logFilesFolder.exists()) {
            val mkdirResult = logFilesFolder.mkdir()
            println("[logger.LoggerStartupListener] (start) create logFilesFolder: " + logFilesFolder.absolutePath + ", success? " + mkdirResult)
        }
        println("[logger.LoggerStartupListener] (start) logFilesFolder: " + logFilesFolder.absolutePath)
        getContext().putProperty("logFilesFolder", logFilesFolder.absolutePath)
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