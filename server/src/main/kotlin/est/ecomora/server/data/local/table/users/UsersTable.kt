package est.ecomora.server.data.local.table.users

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
/**
 * Represents the Users table schema for database storage.
 * Contains columns for user identification and authentication.
 */

object UsersTable: Table("Users") {
    val id: Column<Long> = long("id").autoIncrement()
    val username: Column<String> = varchar("username", 48)
    val password: Column<String> = varchar("password", 48)
    val email: Column<String> = varchar("email", 80)
    val fullName: Column<String> = varchar("fullName",80)
    val phoneNumber: Column<String> = varchar("phoneNumber", length = 130)
    val userRole: Column<String> = varchar("userRole", length = 50)
    val userImage: Column<String> = varchar("profile", 150)

    override val primaryKey = PrimaryKey(id)
}