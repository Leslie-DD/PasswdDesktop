package utils

import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import java.util.Base64.getDecoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {

    private const val TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding"
    private const val ALGORITHM_AES = "AES"
    private const val ALGORITHM_SECURE_RANDOM = "NativePRNGNonBlocking"

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_CBC)
    private val secureRandom: SecureRandom = SecureRandom.getInstance(ALGORITHM_SECURE_RANDOM)

    /**
     * 加密
     *
     * @param secretKey 秘钥
     * @param plainText 明文
     * @return 密文
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(secretKey: String, plainText: String?): ByteArray? {
        if (plainText.isNullOrEmpty()) {
            return null
        }
        return encrypt(getDecoder().decode(secretKey), plainText.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * 加密
     *
     * @param secretKey 秘钥
     * @param plainText 明文
     * @return 密文
     */
    @Synchronized
    @Throws(GeneralSecurityException::class)
    private fun encrypt(secretKey: ByteArray, plainText: ByteArray): ByteArray? {
        // CBC模式需要生成一个16 bytes的initialization vector:
        val startTime = System.currentTimeMillis()
        val iv = getSecureKey(16) ?: return null
        cipher.apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(secretKey, ALGORITHM_AES),
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
     * @param secretKey  秘钥
     * @param cipherText 密文
     * @return 明文
     */
    fun decrypt(secretKey: ByteArray?, cipherText: ByteArray): ByteArray? {
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
                    SecretKeySpec(secretKey, ALGORITHM_AES),
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

    fun getSecureKey(numBytes: Int): ByteArray? {
        return try {
            secureRandom.generateSeed(numBytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }
}
