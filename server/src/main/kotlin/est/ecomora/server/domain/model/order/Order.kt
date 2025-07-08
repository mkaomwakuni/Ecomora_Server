package est.ecomora.server.domain.model.order

import kotlinx.serialization.Serializable

@Serializable
data class Order (
    val id: Long,
    val userId: Int,
    val productIds: Int,
    val totalQuantity: String,
    val totalSum: Int,
    val status: String,
    val indicatorColor: String,
    val paymentType: String,
    val trackingNumber: String,
    val orderDate: String
)