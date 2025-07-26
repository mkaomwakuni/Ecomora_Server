package est.ecomora.server.domain.model.login

import est.ecomora.server.domain.model.users.Users
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val userName: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val userRole: String = "user",
    val imageUrl: String? = null,
    val profile: String? = null
)

@Serializable
data class LoginResponse (
    val message: String,
    val user: Users,
    val token: String? = null
)