package est.ecomora.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

object ValidationRules {
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult(false, listOf("Email is required"))
        }
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        if (!emailRegex.matches(email)) {
            return ValidationResult(false, listOf("Invalid email format"))
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String?): ValidationResult {
        val errors = mutableListOf<String>()
        if (password.isNullOrBlank()) {
            errors.add("Password is required")
        } else {
            if (password.length < 8) {
                errors.add("Password must be at least 8 characters long")
            }
            if (!password.any { it.isUpperCase() }) {
                errors.add("Password must contain at least one uppercase letter")
            }
            if (!password.any { it.isLowerCase() }) {
                errors.add("Password must contain at least one lowercase letter")
            }
            if (!password.any { it.isDigit() }) {
                errors.add("Password must contain at least one digit")
            }
        }
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateUsername(username: String?): ValidationResult {
        if (username.isNullOrBlank()) {
            return ValidationResult(false, listOf("Username is required"))
        }
        if (username.length < 3) {
            return ValidationResult(false, listOf("Username must be at least 3 characters long"))
        }
        if (username.length > 50) {
            return ValidationResult(false, listOf("Username must be less than 50 characters"))
        }
        val usernameRegex = Regex("^[a-zA-Z0-9_.-]+$")
        if (!usernameRegex.matches(username)) {
            return ValidationResult(
                false,
                listOf("Username can only contain letters, numbers, dots, hyphens, and underscores")
            )
        }
        return ValidationResult(true)
    }

    fun validatePhoneNumber(phoneNumber: String?): ValidationResult {
        if (phoneNumber.isNullOrBlank()) {
            return ValidationResult(false, listOf("Phone number is required"))
        }
        val phoneRegex = Regex("^\\+?[1-9]\\d{1,14}$")
        if (!phoneRegex.matches(phoneNumber.replace(" ", "").replace("-", ""))) {
            return ValidationResult(false, listOf("Invalid phone number format"))
        }
        return ValidationResult(true)
    }

    fun validateRequired(value: String?, fieldName: String): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult(false, listOf("$fieldName is required"))
        }
        return ValidationResult(true)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.validateAndRespond(
    validations: List<ValidationResult>
): Boolean {
    val allErrors = validations.flatMap { it.errors }
    if (allErrors.isNotEmpty()) {
        call.respond(
            HttpStatusCode.BadRequest,
            mapOf(
                "success" to false,
                "errors" to allErrors
            )
        )
        return false
    }
    return true
}