package est.ecomora.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.slf4j.LoggerFactory

fun Application.configureLogging() {
    // Basic logging configuration - CallLogging plugin will be configured separately if needed
}

object AppLogger {
    private val logger = LoggerFactory.getLogger("EcomoraServer")
    
    fun info(message: String, vararg args: Any?) {
        logger.info(message, *args)
    }
    
    fun debug(message: String, vararg args: Any?) {
        logger.debug(message, *args)
    }
    
    fun warn(message: String, vararg args: Any?) {
        logger.warn(message, *args)
    }
    
    fun error(message: String, throwable: Throwable? = null, vararg args: Any?) {
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message, *args)
        }
    }
    
    fun trace(message: String, vararg args: Any?) {
        logger.trace(message, *args)
    }
}

object SecurityLogger {
    private val logger = LoggerFactory.getLogger("Security")
    
    fun logLoginAttempt(email: String, success: Boolean, ipAddress: String?) {
        if (success) {
            logger.info("Successful login attempt for user: {} from IP: {}", email, ipAddress)
        } else {
            logger.warn("Failed login attempt for user: {} from IP: {}", email, ipAddress)
        }
    }
    
    fun logRegistration(email: String, ipAddress: String?) {
        logger.info("New user registration: {} from IP: {}", email, ipAddress)
    }
    
    fun logUnauthorizedAccess(path: String, ipAddress: String?, userAgent: String?) {
        logger.warn("Unauthorized access attempt to: {} from IP: {} with User-Agent: {}", path, ipAddress, userAgent)
    }
    
    fun logPasswordChange(userId: String, ipAddress: String?) {
        logger.info("Password changed for user ID: {} from IP: {}", userId, ipAddress)
    }
    
    fun logTokenGeneration(userId: String) {
        logger.debug("JWT token generated for user ID: {}", userId)
    }
    
    fun logTokenValidation(userId: String?, success: Boolean) {
        if (success) {
            logger.debug("JWT token validated successfully for user ID: {}", userId)
        } else {
            logger.warn("JWT token validation failed for user ID: {}", userId)
        }
    }
}

object DatabaseLogger {
    private val logger = LoggerFactory.getLogger("Database")
    
    fun logQuery(operation: String, table: String, userId: String? = null) {
        logger.debug("Database operation: {} on table: {} by user: {}", operation, table, userId)
    }
    
    fun logError(operation: String, table: String, error: String, userId: String? = null) {
        logger.error("Database error during {}: {} on table: {} by user: {}", operation, error, table, userId)
    }
    
    fun logConnection(status: String) {
        logger.info("Database connection: {}", status)
    }
}

object ApiLogger {
    private val logger = LoggerFactory.getLogger("API")
    
    fun logRequest(method: String, path: String, userId: String? = null, ipAddress: String? = null) {
        logger.info("API Request: {} {} by user: {} from IP: {}", method, path, userId, ipAddress)
    }
    
    fun logResponse(method: String, path: String, status: HttpStatusCode, duration: Long, userId: String? = null) {
        logger.info("API Response: {} {} - {} ({}ms) for user: {}", method, path, status, duration, userId)
    }
    
    fun logError(method: String, path: String, error: String, userId: String? = null) {
        logger.error("API Error: {} {} - {} for user: {}", method, path, error, userId)
    }
    
    fun logValidationError(method: String, path: String, errors: List<String>, userId: String? = null) {
        logger.warn("API Validation Error: {} {} - {} for user: {}", method, path, errors.joinToString(", "), userId)
    }
}