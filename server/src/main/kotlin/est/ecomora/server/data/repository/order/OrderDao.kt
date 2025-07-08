package est.ecomora.server.data.repository.order

import est.ecomora.server.domain.model.order.Order

interface OrderDao {
    suspend fun insertOrder(
        userId: Int,
        productIds: Int,
        totalQuantity: String,
        totalSum: Int,
        status: String,
        paymentType: String,
        indicatorColor: String,
        trackingNumber: String,
        orderDate: String
    ): Order?

    suspend fun deleteOrderById(id: Long): Int

    suspend fun updateOrderStatus(
        id: Long,
        status: String,
        currentTimestamp: String
    ): Int

    suspend fun getAllOrders(): List<Order>
    suspend fun getOrderById(id: Long): Order?

    suspend fun getAllOrdersByUserId(id: Int): List<Order>
}