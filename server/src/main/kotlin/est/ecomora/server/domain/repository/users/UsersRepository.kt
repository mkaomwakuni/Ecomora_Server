package est.ecomora.server.domain.repository.users

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.users.UsersTable
import est.ecomora.server.data.repository.users.UsersDao
import est.ecomora.server.domain.model.users.Users
import est.ecomora.server.domain.service.PasswordService
import est.ecomora.server.plugins.AppLogger
import est.ecomora.server.plugins.SecurityLogger
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class UsersRepositoryImpl : UsersDao {

    /**
     * Converts a database result row to a Users domain model object.
     */
    private fun rowToResponse(row: ResultRow): Users? {
        return try {
            Users(
                id = row[UsersTable.id],
                userName = row[UsersTable.username],
                email = row[UsersTable.email],
                phoneNumber = row[UsersTable.phoneNumber],
                userRole = row[UsersTable.userRole],
                fullName = row[UsersTable.fullName],
                imageUrl = row[UsersTable.imageUrl],
                profile = row[UsersTable.profile],
                createdAt = row[UsersTable.createdAt],
                updatedAt = row[UsersTable.updatedAt]
            )
        } catch (e: Exception) {
            AppLogger.error("Error converting database row to User object", e)
            null
        }
    }

    override suspend fun insertUser(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        userRole: String,
        usersImage: String,
        fullName: String
    ): Users? {
        return try {
            var insertStatement: InsertStatement<Number>? = null
            val hashedPassword = PasswordService.hashPassword(password)
            val currentTime = System.currentTimeMillis().toString()

            DatabaseFactory.dbQuery {
                insertStatement = UsersTable.insert { users ->
                    users[UsersTable.username] = username
                    users[UsersTable.email] = email
                    users[UsersTable.password] = hashedPassword
                    users[UsersTable.phoneNumber] = phoneNumber
                    users[UsersTable.userRole] = userRole
                    users[UsersTable.imageUrl] = usersImage
                    users[UsersTable.fullName] = fullName
                    users[UsersTable.profile] = ""
                    users[UsersTable.createdAt] = currentTime
                    users[UsersTable.updatedAt] = currentTime
                }
            }

            insertStatement?.resultedValues?.get(0)?.let { row ->
                SecurityLogger.logRegistration(email, null)
                rowToResponse(row)
            }
        } catch (e: Exception) {
            AppLogger.error("Error inserting user: $username", e)
            null
        }
    }

    override suspend fun signUpUser(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        userRole: String,
        usersImage: String,
        fullName: String
    ): Users? {
        return insertUser(username, password, email, phoneNumber, userRole, usersImage, fullName)
    }

    override suspend fun login(
        email: String,
        password: String
    ): Users? {
        return try {
            DatabaseFactory.dbQuery {
                val result = UsersTable.select {
                    UsersTable.email eq email
                }.singleOrNull()

                result?.let { row ->
                    val storedPassword = row[UsersTable.password]
                    if (PasswordService.verifyPassword(password, storedPassword)) {
                        SecurityLogger.logLoginAttempt(email, true, null)

                        if (PasswordService.isLegacyPassword(storedPassword)) {
                            try {
                                // password to BCrypt
                                val newBcryptHash = PasswordService.hashPassword(password)
                                UsersTable.update({ UsersTable.email eq email }) { user ->
                                    user[UsersTable.password] = newBcryptHash
                                    user[UsersTable.updatedAt] =
                                        System.currentTimeMillis().toString()
                                }
                                AppLogger.info(
                                    "Migrated password from PBKDF2 to BCrypt for user: {}",
                                    email
                                )
                            } catch (e: Exception) {
                                AppLogger.error(
                                    "Failed to migrate password to BCrypt for user: $email",
                                    e
                                )
                            }
                        }

                        rowToResponse(row)
                    } else {
                        SecurityLogger.logLoginAttempt(email, false, null)
                        null
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error during login for user: $email", e)
            SecurityLogger.logLoginAttempt(email, false, null)
            null
        }
    }

    override suspend fun getAllUsers(): List<Users>? {
        return try {
            DatabaseFactory.dbQuery {
                UsersTable.selectAll().mapNotNull { row ->
                    rowToResponse(row)
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error fetching all users", e)
            null
        }
    }

    override suspend fun getUsersById(id: Long): Users? {
        return try {
            DatabaseFactory.dbQuery {
                UsersTable.select {
                    UsersTable.id.eq(id)
                }.map { row ->
                    rowToResponse(row)
                }.singleOrNull()
            }
        } catch (e: Exception) {
            AppLogger.error("Error fetching user by ID: $id", e)
            null
        }
    }

    override suspend fun deleteUserById(id: Long): Int {
        return try {
            DatabaseFactory.dbQuery {
                UsersTable.deleteWhere {
                    UsersTable.id.eq(id)
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error deleting user by ID: $id", e)
            0
        }
    }

    override suspend fun updateUserInfo(
        id: Long,
        username: String,
        email: String,
        fullName: String,
        phoneNumber: String
    ): Int {
        return try {
            val currentTime = System.currentTimeMillis().toString()
            DatabaseFactory.dbQuery {
                UsersTable.update({ UsersTable.id.eq(id) }) { user ->
                    user[UsersTable.username] = username
                    user[UsersTable.email] = email
                    user[UsersTable.fullName] = fullName
                    user[UsersTable.phoneNumber] = phoneNumber
                    user[UsersTable.updatedAt] = currentTime
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error updating user info for ID: $id", e)
            0
        }
    }

    override suspend fun updateUserImage(id: Long, usersImage: String): Int {
        return try {
            val currentTime = System.currentTimeMillis().toString()
            DatabaseFactory.dbQuery {
                UsersTable.update({ UsersTable.id.eq(id) }) { user ->
                    user[UsersTable.imageUrl] = usersImage
                    user[UsersTable.updatedAt] = currentTime
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error updating user image for ID: $id", e)
            0
        }
    }

    override suspend fun updateUsers(
        id: Long,
        username: String,
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        userImage: String
    ): Int {
        return try {
            val hashedPassword = PasswordService.hashPassword(password)
            val currentTime = System.currentTimeMillis().toString()
            DatabaseFactory.dbQuery {
                UsersTable.update({ UsersTable.id.eq(id) }) { user ->
                    user[UsersTable.username] = username
                    user[UsersTable.email] = email
                    user[UsersTable.password] = hashedPassword
                    user[UsersTable.fullName] = fullName
                    user[UsersTable.phoneNumber] = phoneNumber
                    user[UsersTable.imageUrl] = userImage
                    user[UsersTable.updatedAt] = currentTime
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Error updating user for ID: $id", e)
            0
        }
    }
}