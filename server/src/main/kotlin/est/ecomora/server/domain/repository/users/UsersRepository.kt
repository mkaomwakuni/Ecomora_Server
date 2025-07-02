package est.ecomora.server.domain.repository.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.JWTVerifier
import com.auth0.jwt.interfaces.Payload
import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.users.UsersTable
import est.ecomora.server.data.repository.users.UsersDao
import est.ecomora.server.domain.model.users.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.security.MessageDigest
import java.util.Date


class UsersRepositoryImpl : UsersDao {
    private val jwtSecret: String = "secret"
    private val jwtAudience: String = "jwtAudience"
    private val jwtIssuer: String = "issuer"
    /**
     * Converts a database result row to a Users domain model object.
     *
     * @param row The result row from the database query
     * @return A Users object if the row is not null, otherwise null
     */
    private fun rowToResponse(row: ResultRow): Users? {
        if (row == null) {
            return null
        }
        else {
            return Users(
                id = row[UsersTable.id],
                username = row[UsersTable.username],
                password = row[UsersTable.password],
                email = row[UsersTable.email],
                phoneNumber = row[UsersTable.phoneNumber],
                userRole = row[UsersTable.userRole],
                fullName = row[UsersTable.fullName]
            )
        }
    }


    /**
     * Converts a database result row to a Users domain model object.
     *
     * @param row The result row from the database query
     * @return A Users object if the row is not null, otherwise null
     */
    override suspend fun insertUser(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        userRole: String,
        usersImage: String,
        fullName: String
    ): Users? {
        var argumentQueries: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            argumentQueries = UsersTable.insert { users ->
                users[UsersTable.username] = username
                users[UsersTable.email] = email
                users[UsersTable.password] = hashedPasskey(password)
                users[UsersTable.phoneNumber] = phoneNumber
                users[UsersTable.userRole] = userRole
                users[UsersTable.userImage] = usersImage
                users[UsersTable.fullName] = fullName
            }
        }
        return rowToResponse(argumentQueries?.resultedValues?.get(0)!!)
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
       var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = UsersTable.insert { users->
                users[UsersTable.username] = username
                users[UsersTable.email] = email
                users[UsersTable.password] = hashedPasskey(password)
                users[UsersTable.phoneNumber] = phoneNumber
                users[UsersTable.userRole] = userRole
                users[UsersTable.userImage] = usersImage
                users[UsersTable.fullName] = fullName
            }
        }
        return rowToResponse(arguments?.resultedValues?.get(0)!!)
    }


    override suspend fun login(
        email: String,
        password: String
    ): Users? {
        var user: Users? = null
        transaction {
            val result = UsersTable.select {
                UsersTable.email eq email
            }.singleOrNull()
            result?.let { row ->
                val storedPasskey = row[UsersTable.password]
                val decodedPassKey = validateJwtTokenTicket(storedPasskey)
                if (verifyPass(password, storedPasskey)) {
                    user = rowToResponse(row)
                }
            }
        }
        return user
    }

    override suspend fun getAllUsers(): List<Users>? =
        DatabaseFactory.dbQuery {
            UsersTable.selectAll().mapNotNull {
                rowToResponse(it)
            }
        }

    override suspend fun getUsersById(id: Long): Users? =
        DatabaseFactory.dbQuery {
            UsersTable.select {
                UsersTable.id.eq(id)
            }.map {
                rowToResponse(it)
            }.singleOrNull()
        }


    override suspend fun deleteUserById(id: Long): Int =
        DatabaseFactory.dbQuery {
            UsersTable.deleteWhere{
                UsersTable.id.eq(id)
            }
        }

    override suspend fun updateUserInfo(
        id: Long,
        username: String,
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String
    ): Int =
        DatabaseFactory.dbQuery {
            UsersTable.update ({UsersTable.id.eq(id)}) { user ->
                user[UsersTable.id] = id
                user[UsersTable.username] = username
                user[UsersTable.email] = email
                user[UsersTable.fullName] = fullName
                user[UsersTable.phoneNumber] = phoneNumber
                user[UsersTable.password] = hashedPasskey(password)
            }

    }

    override suspend fun updateUserImage(id: Long, usersImage: String): Int =
        DatabaseFactory.dbQuery {
            UsersTable.update({UsersTable.id.eq(id)}) { user ->
                user[UsersTable.userImage] = usersImage
            }
        }

    override suspend fun updateUsers(
        id: Long,
        username: String,
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String
    ): Int  =
        DatabaseFactory.dbQuery {
            UsersTable.update ( { UsersTable.id.eq(id) }) { user ->
                user[UsersTable.id] = id
                user[UsersTable.username] = username
                user[UsersTable.email] = email
                user[UsersTable.fullName] = fullName
                user[UsersTable.phoneNumber] = phoneNumber
            }
        }

    private val jwtVerifier: JWTVerifier = JWT.require(
        Algorithm.HMAC256(jwtSecret)
    )
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .build()

    private fun generateJwtTokenTicket(passkey: String): String {
        val expirationDate = System.currentTimeMillis() + 300000
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withSubject(passkey)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    fun validateJwtTokenTicket(token: String): String? {
        return try {
            val payload: Payload = jwtVerifier.verify(token)
            payload.subject
        } catch (e: TokenExpiredException) {
            println("Token Expired: ${e.message}")
            null
        } catch (e: JWTVerificationException) {
            println("JWT Verification failed: ${e.message}")
            null
        }
    }

    private fun hashedPasskey(password: String): String {
        val gist = MessageDigest.getInstance("SHA-256")
        val hashBytes = gist.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPass(passkeyAdded: String, hashedKey: String): Boolean {
        val hashPassKeyProvided = hashedPasskey(passkeyAdded)
        return  hashPassKeyProvided == hashedKey
    }
}