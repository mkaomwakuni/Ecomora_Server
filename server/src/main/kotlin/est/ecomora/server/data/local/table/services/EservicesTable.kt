package est.ecomora.server.data.local.table.services

import org.jetbrains.exposed.sql.Table

object EservicesTable: Table("Services") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val price = long("price")
    val offered = long("offered")
    val category = varchar("category", 255)
    val imageUrl = varchar("imageUrl", 255)
    val isVisible = bool("isVisible")
    val createdAt = varchar("createdAt", 255)
    val updatedAt = varchar("updatedAt",255)

    override val primaryKey = PrimaryKey(id)
}