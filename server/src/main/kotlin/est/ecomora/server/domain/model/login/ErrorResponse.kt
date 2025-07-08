package est.ecomora.server.domain.model.login

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val success: Boolean = false,
    val message: String? = null
)

@Serializable
data class SuccessResponse<T>(
    val message: String,
    val data: T? = null,
    val success: Boolean = true
)