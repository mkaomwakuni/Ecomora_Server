package est.tunzo.cyberpros.server.domain.repository.users

import est.tunzo.cyberpros.server.data.local.table.DatabaseFactory
import est.tunzo.cyberpros.server.data.local.table.users.UsersTable
import est.tunzo.cyberpros.server.data.repository.users.UsersDao
import est.tunzo.cyberpros.server.domain.model.users.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement





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
                email = row[UsersTable.email]
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
        email: String
    ): Users? {
       var  argumentQueries: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            argumentQueries = UsersTable.insert { users ->
                users[UsersTable.username] = username
                users[UsersTable.email] = email
                users[UsersTable.password] = password
            }
        }
        return  rowToResponse(argumentQueries?.resultedValues?.get(0)!!)
    }
}