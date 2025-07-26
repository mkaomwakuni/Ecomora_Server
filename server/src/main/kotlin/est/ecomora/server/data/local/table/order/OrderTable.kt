package est.ecomora.server.data.local.table.order

import est.ecomora.server.data.local.table.users.UsersTable
import org.jetbrains.exposed.sql.Table

object OrderTable: Table("Orders"){
    val id = long("id").autoIncrement()
    val userId = long("userId").references(UsersTable.id).index()
    val productIds = varchar("productIds", 2000) 
    val totalQuantity = integer("totalQuantity") 
    val totalSum = long("totalSum") 
    val status = varchar("status", 100)
    val paymentType = varchar("paymentType", 100)
    val trackingNumber = varchar("trackingNumber", 100)
    val orderDate = varchar("orderDate", 100)
    val indicatorColor = varchar("indicatorColor", 50)

    override val primaryKey = PrimaryKey(id)
}