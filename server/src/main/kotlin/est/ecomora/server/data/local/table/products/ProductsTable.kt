package est.ecomora.server.data.local.table.products


import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


object ProductsTable: Table ("Products") {
    val id: Column<Long> = long("id").autoIncrement()
    val name: Column<String> = varchar("name", length = 1000)
    val description: Column<String> = varchar("description", length = 1000)
    val price: Column<Long> = long("price")
    val imageUrl: Column<String> = varchar("imageUrl", length = 255)
    val categoryName: Column<String> = varchar("category_name", length = 500)
    val categoryId: Column<Long> = long("category_id")
    val createdDate: Column<String> = varchar("created_date", length = 500)
    val updatedDate: Column<String> = varchar("updated_date", length = 500)
    val totalStock: Column<Long> = long("total_stock")
    val brand: Column<String> = varchar("brand", length = 1000)
    val isAvailable: Column<Boolean> = bool("is_available")
    val discount: Column<Long> = long("discount")
    val promotion: Column<String> = varchar("promotion", length = 2000)
    val productRating: Column<Double> = double("product_rating")
    val sold: Column<Long> = long("sold").default(0)

    val isFeatured: Column<Boolean> = bool("isfeatured")

    val color: Column<String> = varchar("color", length = 500)

    override val primaryKey = PrimaryKey(id)
}
