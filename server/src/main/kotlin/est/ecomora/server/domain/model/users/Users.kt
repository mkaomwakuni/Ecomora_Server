package est.ecomora.server.domain.model.users

import kotlinx.serialization.Serializable

/**
 * Represents a user in the Ecomora system.
 *
 * @property id Unique identifier for the user
 * @property username User's chosen username
 * @property email User's email address
 * @property password Hashed user password
 */
@Serializable
data class Users (
    val id : Long,
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val userRole: String,
    val userImage: String
)