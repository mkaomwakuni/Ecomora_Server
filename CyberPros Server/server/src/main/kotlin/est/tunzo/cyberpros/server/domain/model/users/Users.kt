package est.tunzo.cyberpros.server.domain.model.users

import kotlinx.serialization.Serializable

@Serializable
data class Users (
    val id : Long,
    val username: String,
    val email: String,
    val password: String,
)