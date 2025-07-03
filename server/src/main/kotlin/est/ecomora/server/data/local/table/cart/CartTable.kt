package est.ecomora.server.data.local.table.cart

import est.ecomora.server.data.local.table.users.UsersTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object CartTable: Table("Cart") {
    val cartId: Column<Int> = integer("cartId").autoIncrement()
    val productId: Column<Long> = long("productId")
    val quality: Column<Int> = integer("quantity")
    val userId: Column<Long> = long("userId").references(UsersTable.id)

    override val primaryKey = PrimaryKey(cartId )

}