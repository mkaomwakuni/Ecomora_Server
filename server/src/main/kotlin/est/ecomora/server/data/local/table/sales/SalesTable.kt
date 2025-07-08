package est.ecomora.server.data.local.table.sales

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object SalesTable : Table("Sales") {
    val id: Column<Long> = long("id").autoIncrement()
    val userId: Column<Int> = integer("user_id")
    val itemId: Column<Long> = long("item_id")
    val itemType: Column<String> = varchar("item_type", length = 50) // "product", "service", or "print"
    val itemName: Column<String> = varchar("item_name", length = 1000)
    val quantity: Column<Int> = integer("quantity")
    val unitPrice: Column<Long> = long("unit_price")
    val totalAmount: Column<Long> = long("total_amount")
    val paymentType: Column<String> = varchar("payment_type", length = 100)
    val saleDate: Column<String> = varchar("sale_date", length = 500)
    val timestamp: Column<Long> = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}