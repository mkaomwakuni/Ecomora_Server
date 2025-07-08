package est.ecomora.server.domain.service

import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

object PasswordService {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 32

    /**
     * Hashes a plain text password using PBKDF2
     * @param plainPassword The plain text password to hash
     * @return The hashed password with salt (base64 encoded)
     */
    fun hashPassword(plainPassword: String): String {
        val salt = generateSalt()
        val hash = hashPassword(plainPassword, salt)
        return "${Base64.getEncoder().encodeToString(salt)}:${
            Base64.getEncoder().encodeToString(hash)
        }"
    }

    /**
     * Verifies a plain text password against a hashed password
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password with salt to verify against
     * @return True if the password matches, false otherwise
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            val parts = hashedPassword.split(":")
            if (parts.size != 2) return false

            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])
            val actualHash = hashPassword(plainPassword, salt)

            expectedHash.contentEquals(actualHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}