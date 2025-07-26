package est.ecomora.server.data.local.table.services

import org.jetbrains.exposed.sql.Table

object EservicesTable: Table("Services") {
    val id = long("id").autoIncrement()
    val userId = long("userid").index()
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val price = long("price")
    val offered = long("offered").default(0)
    val sold = long("sold").default(0)
    val categoryName = varchar("categoryName", 255)
    val categoryId = long("categoryId")
    val imageUrl = varchar("imageurl", 255)
    val isVisible = bool("isvisible")
    val createdAt = varchar("createdat", 255)
    val updatedAt = varchar("updatedat", 255)
    val discount = long("discount").default(0)
    val promotion = varchar("promotion", 500).default("")

    override val primaryKey = PrimaryKey(id)
}