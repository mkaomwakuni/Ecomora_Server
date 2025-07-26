package est.ecomora.server.routes

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val success: Boolean = false
)

@Serializable
data class SuccessResponse(
    val message: String,
    val success: Boolean = true
)