package config

import model.ScreenOrientation
import model.Theme
import org.liangguo.ktpref.KtPref
import platform.desktop.storageDir

object LocalPref {

    private val apiConfig by lazy { KtPref("ApiConfig", storageDir) }

    init {
        KtPref.initialize(storageDir)
    }

    var theme: Theme
        set(value) {
            KtPref.default.put(Theme.className, value.className)
        }
        get() = Theme.Themes.find { KtPref.default.getString(Theme.className) == it.className }
            ?: Theme.Default

    var screenOrientation: ScreenOrientation
        set(value) {
            KtPref.default.put(ScreenOrientation.className, value.className)
        }
        get() = ScreenOrientation.ScreenOrientations.find { KtPref.default.getString(ScreenOrientation.className) == it.className }
            ?: ScreenOrientation.Default

    var accessToken: String
        set(value) {
            apiConfig.put("access_token", value)
        }
        get() = apiConfig.getString("access_token", "")

    var secretKey: String
        set(value) {
            apiConfig.put("SecretKey", value)
        }
        get() = apiConfig.getString("SecretKey", "这是秘钥")

    var baiduAppKey: String
        set(value) {
            apiConfig.put("BaiduAppKey", value)
        }
        get() = apiConfig.getString("BaiduAppKey", "")

    var baiduAppSecret: String
        set(value) {
            apiConfig.put("BaiduAppSecret", value)
        }
        get() = apiConfig.getString("BaiduAppSecret", "")

}

private val Any.className: String
    get() = javaClass.name.let { if (it.contains('.')) it.substringAfterLast(".") else it }