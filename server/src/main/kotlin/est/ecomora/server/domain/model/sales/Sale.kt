package est.ecomora.server.domain.model.sales

import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: Long,
    val userId: Int,
    val itemId: Long,
    val itemType: String, // "product", "service", or "print"
    val itemName: String,
    val quantity: Int,
    val unitPrice: Long,
    val totalAmount: Long,
    val paymentType: String,
    val saleDate: String,
    val timestamp: Long
)