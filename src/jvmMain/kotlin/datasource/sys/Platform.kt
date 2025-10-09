package datasource.sys

import java.util.Locale


var currentOS: OS = OS.UNKNOWN

actual fun getOperatingSystem(): OS {
    if (currentOS != OS.UNKNOWN) {
        return currentOS
    }

    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        osName.contains("win") -> OS.WINDOWS
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.LINUX
        osName.contains("mac") -> OS.MAC_OS
        else -> OS.UNKNOWN
    }.apply {
        currentOS = this
    }

}

actual val currentPlatform: String
    get() = "Desktop (${System.getProperty("os.name")})"