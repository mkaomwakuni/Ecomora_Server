package est.ecomora.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

object SecurityConfig {
    val jwtSecret = System.getenv("JWT_SECRET") ?: run {
        AppLogger.warn("JWT_SECRET not set, using default secret for development. THIS IS NOT SECURE FOR PRODUCTION!")
        "dev-secret-key-please-change-in-production-min-256-bits"
    }
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "ecomora-api"
    val jwtDomain = System.getenv("JWT_DOMAIN") ?: "https://ecomora.com/"
    val jwtRealm = System.getenv("JWT_REALM") ?: "ecomora server"
    val jwtExpirationTime = (System.getenv("JWT_EXPIRATION_HOURS")?.toLongOrNull() ?: 24) * 60 * 60 * 1000 // hours to milliseconds
}

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
    
    authentication {
        jwt {
            realm = SecurityConfig.jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(SecurityConfig.jwtSecret))
                    .withAudience(SecurityConfig.jwtAudience)
                    .withIssuer(SecurityConfig.jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(SecurityConfig.jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is not valid or has expired"))
            }
        }
    }
    
    routing {
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }

    AppLogger.info("Security configuration completed")
}
