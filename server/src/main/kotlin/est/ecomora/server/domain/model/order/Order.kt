package est.ecomora.server.domain.model.order

import kotlinx.serialization.Serializable

@Serializable
data class Order (
    val id: Long,
    val userId: Long,
    val productIds: String,
    val totalQuantity: Int,
    val totalSum: Long,
    val status: String,
    val indicatorColor: String,
    val paymentType: String,
    val trackingNumber: String,
    val orderDate: String
)