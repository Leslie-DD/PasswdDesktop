package datasource.sys

enum class OS {
    WINDOWS,
    LINUX,
    MAC_OS,
    UNKNOWN
}

expect fun getOperatingSystem(): OS

expect val currentPlatform: String