package est.ecomora.server.data.local.table.users

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
/**
 * Represents the Users table schema for database storage.
 * Contains columns for user identification and authentication.
 */

object UsersTable: Table("Users") {
    val id: Column<Long> = long("id").autoIncrement()
    val username: Column<String> = varchar("username", 500)
    val password: Column<String> = varchar("password", 500)
    val email: Column<String> = varchar("email", 1000)
    val fullName: Column<String> = varchar("fullname", 1000)
    val phoneNumber: Column<String> = varchar("phonenumber", length = 500)
    val userRole: Column<String> = varchar("userrole", length = 100)
    val imageUrl: Column<String> = varchar("imageurl", 500)
    val profile: Column<String> = varchar("profile", 2000).default("")
    val createdAt: Column<String> = varchar("createdat", 500)
    val updatedAt: Column<String> = varchar("updatedat", 500)

    override val primaryKey = PrimaryKey(id)
}