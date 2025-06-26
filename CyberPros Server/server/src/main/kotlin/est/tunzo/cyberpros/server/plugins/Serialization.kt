package est.tunzo.cyberpros.server.plugins

import io.ktor.server.application.Application
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
/**
 * Configures JSON serialization settings using kotlinx.serialization library.
 * Provides a pre-configured [Json] instance with common serialization options.
 */
import kotlinx.serialization.json.Json
/**
 * Configures serialization and routing for the Ktor application.
 *
 * This function sets up JSON serialization with pretty printing and lenient parsing,
 * and adds a sample endpoint that responds with a simple JSON object.
 *
 * @see ContentNegotiation
 * @see Json
 */

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}