package utils

import entity.Passwd
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object DecodeUtils {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun MutableList<Passwd>.decodePasswds(
        secretKey: String
    ): MutableList<Passwd> {
        val secretKeyByteArray = Base64.getDecoder().decode(secretKey)
        val passwdsResult: MutableList<Passwd> = arrayListOf()
        forEach { passwdsResult.add(decodePasswd(secretKeyByteArray, it)) }
        return passwdsResult
    }

    fun decodePasswd(
        secretKeyBytes: ByteArray? = null,
        passwd: Passwd
    ): Passwd = try {
        passwd.copy(
            title = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.title),
            usernameString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.usernameString),
            passwordString = AESUtil.decrypt(secretKeyBytes = secretKeyBytes, cipherText = passwd.passwordString)
        )
    } catch (e: Exception) {
        logger.error("(decodePasswd) error ", e)
        passwd
    }
}