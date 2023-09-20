package model.viewmodel

import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import model.Theme
import model.next
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities


class ConfigViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val theme = MutableStateFlow<Theme>(Theme.Default)

    private var systemDark = false
    private val detector: OsThemeDetector = OsThemeDetector.getDetector()

    init {
        updateSystemDark(detector.isDark)
        updateTheme(nextTheme = theme.value)

        detector.registerListener { isDark ->
            logger.info("OsThemeDetector listener: isDark $isDark")
            updateSystemDark(isDark)
            if (theme.value is Theme.Auto) {
                SwingUtilities.invokeLater {
                    updateTheme(isDark, nextTheme = Theme.Auto())
                }
            }
        }
    }

    private fun updateSystemDark(dark: Boolean) {
        logger.debug("(updateSystemDark) dark: $dark")
        systemDark = dark
    }

    fun updateTheme(
        forceDark: Boolean = false,
        nextTheme: Theme = theme.value.next()
    ) {
        logger.debug("(updateTheme) nextTheme: {}. forceDark: {}, systemDark: {}", nextTheme, forceDark, systemDark)
        if (nextTheme is Theme.Auto) {
            nextTheme.dark = forceDark || systemDark
        }
        theme.tryEmit(nextTheme)
    }
}