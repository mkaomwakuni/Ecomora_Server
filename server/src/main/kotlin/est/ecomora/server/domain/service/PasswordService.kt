package est.ecomora.server.domain.service

import at.favre.lib.crypto.bcrypt.BCrypt
import java.util.Base64

/**
 * Password service using BCrypt for secure password hashing
 */
object PasswordService {
    private const val BCRYPT_COST = 12 // Higher cost = more secure but slower

    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray())
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {

            if (hashedPassword.contains(":") && hashedPassword.split(":").size == 2) {

                verifyLegacyPBKDF2Password(plainPassword, hashedPassword)
            } else {

                BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified
            }
        } catch (e: Exception) {
            false
        }
    }


    private fun verifyLegacyPBKDF2Password(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            val parts = hashedPassword.split(":")
            if (parts.size != 2) return false

            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])

            val spec = javax.crypto.spec.PBEKeySpec(
                plainPassword.toCharArray(),
                salt,
                100_000,
                256
            )
            val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val actualHash = factory.generateSecret(spec).encoded

            expectedHash.contentEquals(actualHash)
        } catch (e: Exception) {
            false
        }
    }

    fun isLegacyassword(hashedPassword: String): Boolean {
        return hashedPassword.contains(":") && hashedPassword.split(":").size == 2
    }
}