package est.ecomora.server.data.local.table.category

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("Categories") {
    val id: Column<Long> = long("id").autoIncrement()
    val name: Column<String> = varchar("name", 80).uniqueIndex()
    val description: Column<String> = varchar("description",80)
    val productCount: Column<Long> = long("productCount")
    val isVisible: Column<Boolean> = bool("isVisible")
    val imageUrl: Column<String> = varchar("imageUrl",80)
}