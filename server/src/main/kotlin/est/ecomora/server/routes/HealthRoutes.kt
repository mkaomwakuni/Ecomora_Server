package est.ecomora.server.routes

import est.ecomora.server.APP_VERSION
import est.ecomora.server.IS_PRODUCTION
import est.ecomora.server.plugins.AppLogger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: Long,
    val environment: String,
    val database: String,
    val uptime: String
)

private val startTime = System.currentTimeMillis()

fun Route.healthRoutes() {
    route("/health") {
        get {
            try {
                // Check database connectivity
                val dbStatus = try {
                    transaction {
                        // Simple database query to check connectivity
                        exec("SELECT 1")
                        "connected"
                    }
                } catch (e: Exception) {
                    AppLogger.error("Database health check failed", e)
                    "disconnected"
                }

                val uptime = System.currentTimeMillis() - startTime
                val uptimeSeconds = uptime / 1000
                val uptimeMinutes = uptimeSeconds / 60
                val uptimeHours = uptimeMinutes / 60
                val uptimeString = "${uptimeHours}h ${uptimeMinutes % 60}m ${uptimeSeconds % 60}s"

                val healthResponse = HealthResponse(
                    status = if (dbStatus == "connected") "healthy" else "unhealthy",
                    version = APP_VERSION,
                    timestamp = System.currentTimeMillis(),
                    environment = if (IS_PRODUCTION) "production" else "development",
                    database = dbStatus,
                    uptime = uptimeString
                )

                if (dbStatus == "connected") {
                    call.respond(HttpStatusCode.OK, healthResponse)
                } else {
                    call.respond(HttpStatusCode.ServiceUnavailable, healthResponse)
                }
            } catch (e: Exception) {
                AppLogger.error("Health check failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("status" to "error", "message" to "Health check failed")
                )
            }
        }
    }

    // Simple readiness probe
    get("/ready") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "ready"))
    }

    // Simple liveness probe
    get("/live") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "alive"))
    }
}