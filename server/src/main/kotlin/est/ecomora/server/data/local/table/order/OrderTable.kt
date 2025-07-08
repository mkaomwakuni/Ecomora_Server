package est.ecomora.server.data.local.table.order

import org.jetbrains.exposed.sql.Table

object OrderTable: Table("Orders"){
    val id = long("id").autoIncrement()
    val userId = integer("userId")
    val productIds = integer("productIds")
    val totalQuantity = varchar("totalQuantity", 1000)
    val totalSum = integer("totalSum")
    val status = varchar("status", 1000)
    val paymentType = varchar("paymentType", 1000)
    val trackingNumber = varchar("trackingNumber", 1000)
    val orderDate = varchar("orderDate",1000)
    val indicatorColor = varchar("indicatorColor", 500)

    override val primaryKey = PrimaryKey(id)
}