package est.ecomora.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import est.ecomora.server.domain.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

object SecurityConfig {
    val jwtSecret = System.getenv("JWT_SECRET") ?: run {
        AppLogger.warn("JWT_SECRET not set")
        "dev-secret-key-256-bits"
    }
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "ecomora-api"
    val jwtDomain = System.getenv("JWT_DOMAIN") ?: "https://ecomora.com/"
    val jwtRealm = System.getenv("JWT_REALM") ?: "ecomora server"
    val jwtExpirationTime = (System.getenv("JWT_EXPIRATION_HOURS")?.toLongOrNull() ?: 24) * 60 * 60 * 1000 // hours to milliseconds
}

@Serializable
data class AuthErrorResponse(
    val error: String,
    val message: String,
    val code: String = "UNAUTHORIZED"
)

fun Application.configureSecurity() {
    @Serializable
    data class MySession(val count: Int = 0)

    val isDevelopment =
        environment.config.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: false

    install(Sessions) {
        cookie<MySession>("ECOMORA_SESSION") {
            cookie.extensions["SameSite"] = "strict"
            cookie.secure = !isDevelopment
            cookie.httpOnly = true
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            val jwtConfig = SecurityConfig
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.jwtSecret))
                    .withAudience(jwtConfig.jwtAudience)
                    .withIssuer(jwtConfig.jwtDomain)
                    .build()
            )
            realm = jwtConfig.jwtRealm
            validate { credential ->
                try {
                    val userId = credential.payload.subject
                    val userRole = credential.payload.getClaim("role").asString()

                    if (userId != null && userRole != null) {
                        AppLogger.debug("JWT validation successful for user ID: $userId with role: $userRole")
                        JWTPrincipal(credential.payload)
                    } else {
                        AppLogger.warn("JWT validation failed: Missing userId or userRole in token")
                        null
                    }
                } catch (e: Exception) {
                    AppLogger.error("JWT validation error: ${e.message}", e)
                    null
                }
            }
            challenge { defaultScheme, realm ->
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                val errorMessage = when {
                    authHeader == null -> "Authorization header is missing. Please include 'Authorization: Bearer <token>' header."
                    !authHeader.startsWith("Bearer ") -> "Invalid authorization header format. Use 'Authorization: Bearer <token>'."
                    else -> "Invalid or expired JWT token. Please login again to get a new token."
                }

                AppLogger.warn("JWT authentication failed: $errorMessage")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse(
                        error = "Authentication Required",
                        message = errorMessage,
                        code = "JWT_AUTH_FAILED"
                    )
                )
            }
        }

        // Optional authentication for public endpoints
        jwt("auth-optional") {
            val jwtConfig = SecurityConfig
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.jwtSecret))
                    .withAudience(jwtConfig.jwtAudience)
                    .withIssuer(jwtConfig.jwtDomain)
                    .build()
            )
            realm = jwtConfig.jwtRealm
            validate { credential ->
                try {
                    val userId = credential.payload.subject
                    val userRole = credential.payload.getClaim("role").asString()

                    if (userId != null && userRole != null) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null // Don't log errors for optional auth
                }
            }
        }
    }
    
    routing {
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }

        // JWT validation endpoint for debugging
        get("/api/v1/auth/validate") {
            authenticate("auth-jwt") {
                get {
                    val userId = call.getCurrentUserId()
                    val userRole = call.getCurrentUserRole()
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "valid" to true,
                            "userId" to userId,
                            "userRole" to userRole,
                            "message" to "Token is valid"
                        )
                    )
                }
            }
        }
    }

    AppLogger.info("Security configuration completed")
}

// Extension function to get current user ID from JWT token
fun ApplicationCall.getCurrentUserId(): Long? {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.payload?.subject?.toLongOrNull()

    if (userId != null) {
        AppLogger.debug("Current authenticated user ID: $userId for request: ${request.uri}")
    } else {
        AppLogger.warn("No authenticated user found for request: ${request.uri}")
    }

    return userId
}

// Extension function to get current user role from JWT token
fun ApplicationCall.getCurrentUserRole(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
}

// Extension function to check if user is authenticated
fun ApplicationCall.isAuthenticated(): Boolean {
    return principal<JWTPrincipal>() != null
}
