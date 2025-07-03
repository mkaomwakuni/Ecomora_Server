package est.ecomora.server.data.local.table.prints

import org.jetbrains.exposed.sql.Table

object PrintsTable: Table("Prints") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val price = double("price")
    val imageUrl = varchar("imageUrl", 255)
    val copies = integer("copies")

    override val primaryKey = PrimaryKey(id)
}