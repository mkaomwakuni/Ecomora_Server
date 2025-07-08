package est.ecomora.server.plugins

import est.ecomora.server.domain.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

enum class UserRole(val role: String) {
    ADMIN("admin"),
    USER("user"),
    MODERATOR("moderator")
}

object AuthorizationService {
    fun hasRole(principal: JWTPrincipal?, requiredRole: UserRole): Boolean {
        val userRole = principal?.payload?.getClaim("role")?.asString()
        return when (requiredRole) {
            UserRole.ADMIN -> userRole == UserRole.ADMIN.role
            UserRole.MODERATOR -> userRole == UserRole.ADMIN.role || userRole == UserRole.MODERATOR.role
            UserRole.USER -> userRole in listOf(
                UserRole.ADMIN.role,
                UserRole.MODERATOR.role,
                UserRole.USER.role
            )
        }
    }

    fun getUserId(principal: JWTPrincipal?): String? {
        return principal?.payload?.subject
    }

    fun getUserRole(principal: JWTPrincipal?): String? {
        return principal?.payload?.getClaim("role")?.asString()
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.requireRole(role: UserRole): Boolean {
    val principal = call.principal<JWTPrincipal>()
    if (principal == null) {
        call.respond(
            HttpStatusCode.Unauthorized,
            mapOf("error" to "Authentication required")
        )
        return false
    }

    if (!AuthorizationService.hasRole(principal, role)) {
        call.respond(
            HttpStatusCode.Forbidden,
            mapOf("error" to "Insufficient permissions. Required role: ${role.role}")
        )
        return false
    }

    return true
}

suspend fun PipelineContext<Unit, ApplicationCall>.requireOwnershipOrRole(
    resourceUserId: String,
    fallbackRole: UserRole = UserRole.ADMIN
): Boolean {
    val principal = call.principal<JWTPrincipal>()
    if (principal == null) {
        call.respond(
            HttpStatusCode.Unauthorized,
            mapOf("error" to "Authentication required")
        )
        return false
    }

    val currentUserId = AuthorizationService.getUserId(principal)
    val hasOwnership = currentUserId == resourceUserId
    val hasRequiredRole = AuthorizationService.hasRole(principal, fallbackRole)

    if (!hasOwnership && !hasRequiredRole) {
        call.respond(
            HttpStatusCode.Forbidden,
            mapOf("error" to "Access denied. You can only access your own resources or need ${fallbackRole.role} role.")
        )
        return false
    }

    return true
}

fun Route.authenticated(
    optional: Boolean = false,
    build: Route.() -> Unit
): Route {
    val authType = if (optional) "auth-optional" else "auth-jwt"
    return authenticate(authType, optional = optional, build = build)
}

fun Route.requiresRole(
    role: UserRole,
    build: Route.() -> Unit
): Route {
    return authenticated {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            if (principal == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Authentication required")
                )
                finish()
                return@intercept
            }

            if (!AuthorizationService.hasRole(principal, role)) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Insufficient permissions. Required role: ${role.role}")
                )
                finish()
                return@intercept
            }
        }
        build()
    }
}