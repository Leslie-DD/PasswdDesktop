package utils

import database.DataBase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.Security
import java.util.*
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@OptIn(DelicateCoroutinesApi::class)
object AESUtil {

    private const val TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding"
    private const val ALGORITHM_AES = "AES"
    private const val ALGORITHM_SECURE_RANDOM = "NativePRNGNonBlocking"

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_CBC)
    private val secureRandom: SecureRandom = getSecureRandom()

    private var secretKeyByteArray: ByteArray? = null

    init {
        GlobalScope.launch {
            DataBase.instance.globalSecretKey.collectLatest {
                log.info("secretKey update: $it")
                secretKeyByteArray = getDecoder().decode(it)
            }
        }
    }

    /**
     * 适配 windows 平台 NoSuchAlgorithmException 的问题
     */
    private fun getSecureRandom(): SecureRandom {
        return try {
            SecureRandom.getInstance(ALGORITHM_SECURE_RANDOM)
        } catch (exception: NoSuchAlgorithmException) {
            return SecureRandom().also {
                it.provider["SecureRandom.NativePRNGNonBlocking"] = it.provider["SecureRandom.${it.algorithm}"]
                Security.addProvider(it.provider)
            }
        }
    }

    /**
     * 加密
     *
     * @param secretKeyBytes 秘钥
     * @param plainText 明文
     * @return 密文
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(
        secretKeyBytes: ByteArray? = secretKeyByteArray,
        plainText: String?
    ): String? {
        if (plainText.isNullOrEmpty()) {
            return null
        }
        val encryptByteArray = encrypt(secretKeyBytes, plainText.toByteArray(StandardCharsets.UTF_8))
        return if (encryptByteArray != null) getEncoder().encodeToString(encryptByteArray) else null
    }

    @Synchronized
    @Throws(GeneralSecurityException::class)
    private fun encrypt(
        secretKeyBytes: ByteArray? = secretKeyByteArray,
        plainText: ByteArray
    ): ByteArray? {
        // CBC模式需要生成一个16 bytes的initialization vector:
        val startTime = System.currentTimeMillis()
        val iv = getSecureKey(16) ?: return null
        cipher.apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(secretKeyBytes, ALGORITHM_AES),
                IvParameterSpec(iv)
            )
        }
        // IV不需要保密，把IV和密文一起返回:
        log.info("end encrypt, cost: ${System.currentTimeMillis() - startTime} ms")
        return join(iv, cipher.doFinal(plainText))
    }

    /**
     * 解密
     *
     * @param secretKeyBytes  秘钥
     * @param cipherText 密文
     * @return 明文
     */
    fun decrypt(
        secretKeyBytes: ByteArray? = secretKeyByteArray,
        cipherText: String?
    ): String? {
        if (cipherText.isNullOrBlank()) {
            return null
        }
        val decryptByteArray = decrypt(secretKeyBytes, getDecoder().decode(cipherText))
        return if (decryptByteArray != null) String(decryptByteArray) else cipherText
    }

    private fun decrypt(
        secretKeyBytes: ByteArray? = secretKeyByteArray,
        cipherText: ByteArray
    ): ByteArray? {
        return try {
            // 把input分割成IV和密文:
            val iv = ByteArray(16)
            val data = ByteArray(cipherText.size - 16)
            System.arraycopy(cipherText, 0, iv, 0, 16)
            System.arraycopy(cipherText, 16, data, 0, data.size)
            // 解密:
            Cipher.getInstance(TRANSFORMATION_CBC).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(secretKeyBytes, ALGORITHM_AES),
                    IvParameterSpec(iv)
                )
            }.doFinal(data)
        } catch (e: GeneralSecurityException) {
            null
        }
    }

    private fun join(bs1: ByteArray, bs2: ByteArray): ByteArray {
        val r = ByteArray(bs1.size + bs2.size)
        System.arraycopy(bs1, 0, r, 0, bs1.size)
        System.arraycopy(bs2, 0, r, bs1.size, bs2.size)
        return r
    }

    private fun getSecureKey(numBytes: Int): ByteArray? {
        return try {
            secureRandom.generateSeed(numBytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }
}
