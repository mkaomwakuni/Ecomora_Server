package est.ecomora.server.domain.model.products

import kotlinx.serialization.Serializable

@Serializable
data class Product (
    val id: Long,
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String,
    val categoryId: Long,
    val categoryName: String,
    val createdDate: String,
    val updatedDate: String,
    val totalStock: Long,
    val brand: String,
    val isAvailable: Boolean,
    val discount: Long,
    val promotion: String,
    val productRating: Double,
    val sold: Long = 0
)
