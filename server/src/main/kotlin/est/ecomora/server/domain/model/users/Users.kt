package est.ecomora.server.domain.model.users

import kotlinx.serialization.Serializable

/**
 * Represents a user in the Ecomora system.
 */
@Serializable
data class Users (
    val id : Long,
    val userName: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val userRole: String,
    val imageUrl: String,
    val profile: String,
    val createdAt: String,
    val updatedAt: String
)