package model.viewmodel

import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Theme
import model.action.UiAction
import model.next
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities

class UiConfigViewModel : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var systemDark = false

    private val detector: OsThemeDetector = OsThemeDetector.getDetector()

    private val _theme = MutableStateFlow<Theme>(Theme.Default)
    val theme: StateFlow<Theme> = _theme.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _searchFocus = MutableStateFlow(true)
    val searchFocus: StateFlow<Boolean> = _searchFocus.asStateFlow()

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

    fun onAction(action: UiAction) {
        logger.debug("onAction: {}", action)
        with(action) {
            when (this) {
                is UiAction.WindowVisible -> _windowVisible.value = visible
                is UiAction.FocusOnSearch -> _searchFocus.value = focus
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
        _theme.tryEmit(nextTheme)
    }
}