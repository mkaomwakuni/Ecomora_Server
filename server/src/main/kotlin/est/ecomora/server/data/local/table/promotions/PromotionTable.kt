package est.ecomora.server.data.local.table.promotions

import est.ecomora.server.data.local.table.users.UsersTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object PromotionTable: Table("promotion") {
    val id: Column<Long> = long("id").autoIncrement()
    val userId: Column<Long> = long("userId").references(UsersTable.id).index()
    val title: Column<String> = varchar("title",1000)
    val description: Column<String> = varchar("description", 1000)
    val imageUrl: Column<String> = varchar("imageUrl",500)
    val startDate: Column<Long> = long("startDate")
    val endDate: Column<Long> = long("endDate")
    val enabled: Column<Boolean> = bool("enable")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}