package est.ecomora.server.domain.repository.order

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.order.OrderTable
import est.ecomora.server.data.repository.order.OrderDao
import est.ecomora.server.domain.model.order.Order
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class OrderRepositoryImpl: OrderDao {
    override suspend fun insertOrder(
        userId: Int,
        productIds: Int,
        totalQuantity: String,
        totalSum:Int,
        status: String,
        paymentType: String,
        indicatorColor: String,
        trackingNumber: String,
        orderDate: String
    ): Order? {
        var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = OrderTable.insert { order ->
                order[OrderTable.userId] = userId
                order[OrderTable.productIds] = productIds
                order[OrderTable.totalQuantity] = totalQuantity
                order[OrderTable.totalSum] = totalSum
                order[OrderTable.status] = status
                order[OrderTable.paymentType] = paymentType
                order[OrderTable.trackingNumber] = trackingNumber
                order[OrderTable.indicatorColor] = indicatorColor
                order[OrderTable.orderDate] = orderDate
            }
        }
        return rowToOrder(arguments?.resultedValues?.get(0)!!)
    }

    override suspend fun deleteOrderById(id: Long): Int {
        return DatabaseFactory.dbQuery {
            OrderTable.deleteWhere { OrderTable.id eq id }
        }
    }

    override suspend fun updateOrderStatus(
        id: Long,
        status: String,
        currentTimestamp: String
    ): Int {
        return DatabaseFactory.dbQuery {
            OrderTable.update({ OrderTable.id eq id }) { order ->
                order[OrderTable.status] = status
                order[OrderTable.orderDate] = currentTimestamp
            }
        }
    }

    override suspend fun getAllOrders(): List<Order> {
       return DatabaseFactory.dbQuery {
           OrderTable.selectAll().mapNotNull { rowToOrder(it) }
       }
    }

    override suspend fun getOrderById(id: Long): Order? {
        return DatabaseFactory.dbQuery {
            OrderTable.select { OrderTable.id eq id }
                .map { rowToOrder(it) }
                .singleOrNull()
        }
    }

    override suspend fun getAllOrdersByUserId(id: Int): List<Order> {
        return DatabaseFactory.dbQuery {
            OrderTable.select { OrderTable.userId eq id }
                .mapNotNull { rowToOrder(it) }
        }
    }


    private fun rowToOrder(row: ResultRow): Order? {
        if (row == null) {
            return null
        } else {
            return Order(
                id = row[OrderTable.id],
                userId = row[OrderTable.userId],
                productIds = row[OrderTable.productIds],
                totalQuantity = row[OrderTable.totalQuantity],
                totalSum = row[OrderTable.totalSum],
                status = row[OrderTable.status],
                paymentType = row[OrderTable.paymentType],
                trackingNumber = row[OrderTable.trackingNumber],
                orderDate = row[OrderTable.orderDate],
                indicatorColor = row[OrderTable.indicatorColor]
            )
        }
    }
}