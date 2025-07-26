package est.ecomora.server

import est.ecomora.server.plugins.*
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import java.io.File

fun main() {
    try {
        AppLogger.info("Starting Ecomora Server v${APP_VERSION} on ${SERVER_HOST}:${SERVER_PORT}")
        AppLogger.info("Running in ${if (IS_PRODUCTION) "PRODUCTION" else "DEVELOPMENT"} mode")

        // Ensure upload directory exists
        val uploadDir = File(UPLOAD_DIR)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
            AppLogger.info("Created upload directory: ${uploadDir.absolutePath}")
        }

        embeddedServer(Netty, port = SERVER_PORT, host = SERVER_HOST, module = Application::module)
            .start(wait = true)
    } catch (e: Exception) {
        AppLogger.error("Failed to start server", e)
        throw e
    }
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureDatabases()
    configureSecurity()
    configureRouting()

    // Configure static file serving
    routing {
        staticFiles("uploads", File(UPLOAD_DIR))
        staticFiles("static", File("${STATIC_FILE_ROOT}/static"))
    }
}
