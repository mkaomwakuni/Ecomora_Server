package est.ecomora.server.domain.model.login

import est.ecomora.server.domain.model.users.Users
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse (
    val message: String,
    val users: Users
)