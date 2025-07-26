package est.ecomora.server.plugins

import est.ecomora.server.IS_PRODUCTION
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.uri

/**
 * Configures HTTP-related plugins for the Ktor application.
 *
 * This function sets up:
 * - Security headers
 * - Default headers with custom information
 * - Caching headers for static content
 */
fun Application.configureHTTP() {
    // Security headers
    install(DefaultHeaders) {
        header("X-Engine", "ktor")
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
        header("Referrer-Policy", "strict-origin-when-cross-origin")

        if (IS_PRODUCTION) {
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        }
    }

    // Caching headers for static content
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                ContentType.Text.JavaScript -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                ContentType.Image.PNG, ContentType.Image.JPEG, ContentType.Image.GIF -> 
                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 7 * 24 * 60 * 60))
                else -> {
                    // For API endpoints, prevent caching to avoid user data mixing
                    if (call.request.uri.startsWith("/api/") || call.request.uri.startsWith("/v1/")) {
                        CachingOptions(CacheControl.NoCache(null))
                    } else {
                        null
                    }
                }
            }
        }
    }
    
    AppLogger.info("HTTP configuration completed - Security headers configured")
}