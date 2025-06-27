package est.ecomora.server.domain.service.users

import est.tunzo.cyberpros.server.domain.model.users.Users
import est.tunzo.cyberpros.server.domain.repository.users.UsersRepository

class UsersService(private val usersRepository: UsersRepository) {

    suspend fun createUser(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        userRole: String
    ): Result<Users> {
        return try {
            // Business logic validation
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Required fields cannot be empty"))
            }

            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            val user = usersRepository.insertUser(username, password, email, phoneNumber, userRole)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<Users>> {
        return try {
            val users = usersRepository.getAllUsers()
            if (users != null) {
                Result.success(users)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(id: Long): Result<Users> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("Invalid user ID"))
            }

            val user = usersRepository.getUsersById(id)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(id: Long): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("Invalid user ID"))
            }

            val rowsAffected = usersRepository.deleteUserById(id)
            if (rowsAffected > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("User not found or already deleted"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        id: Long,
        username: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("Invalid user ID"))
            }

            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Required fields cannot be empty"))
            }

            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            val rowsAffected = usersRepository.updateUsers(id, username, email, password)
            if (rowsAffected > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("User not found or update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}