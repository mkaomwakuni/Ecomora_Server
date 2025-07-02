package est.ecomora.server.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.defaultheaders.*

/**
 * Configures HTTP-related plugins for the Ktor application.
 *
 * This function sets up:
 * - Default headers with a custom "X-Engine" header
 * - Caching headers for CSS files with a 24-hour max age cache control
 */
fun Application.configureHTTP() {
   install(DefaultHeaders) {
       header("X-Engine","ktor")
   }
    install(CachingHeaders) {
        options { call, outgoingClient ->
            when(outgoingClient.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(24*60*60))
                else -> null
            }
        }
    }
}