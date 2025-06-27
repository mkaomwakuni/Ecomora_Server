package est.ecomora.server.data.repository.users

import est.ecomora.server.domain.model.users.Users


/**
 * Data Access Object (DAO) interface for managing user-related database operations.
 *
 * @see Users The user model representing the structure of user data
 */
interface UsersDao {

    suspend fun insertUser(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        userRole: String
    ):  Users?

    suspend fun getAllUsers(): List<Users>?
    suspend fun getUsersById(id: Long): Users?
    suspend fun deleteUserById(id: Long): Int
    suspend fun updateUsers(id: Long,username: String,email: String,password: String): Int
}