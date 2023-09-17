package platform.desktop

import app.cash.sqldelight.db.SqlDriver
import platform.Platform
import java.io.File

/**
 * 当前平台是什么
 */
expect val currentPlatform: Platform

/**
 * 由于电脑端和安卓端的文件储存位置不同
 * 所以要基于这个基准文件夹来进行本地储存分类
 */
expect val storageDir: File

expect fun createSqlDriver(): SqlDriver
