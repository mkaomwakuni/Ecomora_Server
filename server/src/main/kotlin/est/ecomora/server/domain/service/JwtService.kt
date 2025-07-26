package est.ecomora.server.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.JWTVerifier
import com.auth0.jwt.interfaces.Payload
import est.ecomora.server.plugins.SecurityConfig
import java.util.*

object JwtService {
    private val jwtVerifier: JWTVerifier = JWT.require(
        Algorithm.HMAC256(SecurityConfig.jwtSecret)
    )
        .withAudience(SecurityConfig.jwtAudience)
        .withIssuer(SecurityConfig.jwtDomain)
        .build()

    // verifier property for authentication configuration
    val verifier: JWTVerifier = jwtVerifier

    /**
     * Generates a JWT token for a user
     * @param userId The user ID to include in the token
     * @param userRole The user role to include in the token
     * @return The generated JWT token
     */
    fun generateToken(userId: String, userRole: String): String {
        val expirationDate = System.currentTimeMillis() + SecurityConfig.jwtExpirationTime
        return JWT.create()
            .withAudience(SecurityConfig.jwtAudience)
            .withIssuer(SecurityConfig.jwtDomain)
            .withSubject(userId)
            .withClaim("role", userRole)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(SecurityConfig.jwtSecret))
    }

    /**
     * Validates a JWT token and returns the payload
     * @param token The JWT token to validate
     * @return The payload if valid, null otherwise
     */
    fun validateToken(token: String): Payload? {
        return try {
            jwtVerifier.verify(token)
        } catch (e: TokenExpiredException) {
            println("Token Expired: ${e.message}")
            null
        } catch (e: JWTVerificationException) {
            println("JWT Verification failed: ${e.message}")
            null
        }
    }

    /**
     * Extracts user ID from a JWT token
     * @param token The JWT token
     * @return The user ID if valid, null otherwise
     */
    fun getUserIdFromToken(token: String): String? {
        return validateToken(token)?.subject
    }

    /**
     * Extracts user role from a JWT token
     * @param token The JWT token
     * @return The user role if valid, null otherwise
     */
    fun getUserRoleFromToken(token: String): String? {
        return validateToken(token)?.getClaim("role")?.asString()
    }
}