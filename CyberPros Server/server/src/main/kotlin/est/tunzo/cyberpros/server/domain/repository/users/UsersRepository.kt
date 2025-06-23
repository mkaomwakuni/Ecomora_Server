package est.tunzo.cyberpros.server.domain.repository.users

import est.tunzo.cyberpros.server.data.local.table.DatabaseFactory
import est.tunzo.cyberpros.server.data.local.table.users.UsersTable
import est.tunzo.cyberpros.server.data.repository.users.UsersDao
import est.tunzo.cyberpros.server.domain.model.users.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update


class UsersRepository : UsersDao {
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
        userRole: String
    ): Users? {
        var argumentQueries: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            argumentQueries = UsersTable.insert { users ->
                users[UsersTable.username] = username
                users[UsersTable.email] = email
                users[UsersTable.password] = password
                users[UsersTable.phoneNumber] = phoneNumber
                users[UsersTable.userRole] = userRole
            }
        }
        return rowToResponse(argumentQueries?.resultedValues?.get(0)!!)
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

    override suspend fun updateUsers(
        id: Long,
        username: String,
        email: String,
        password: String
    ): Int  =
        DatabaseFactory.dbQuery {
            UsersTable.update ( { UsersTable.id.eq(id) }) { user ->
                user[UsersTable.id] = id
                user[UsersTable.username] = username
                user[UsersTable.email] = email
                user[UsersTable.password] = password
            }
        }
}