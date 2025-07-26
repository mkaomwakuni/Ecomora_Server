package est.ecomora.server.domain.model.services

import kotlinx.serialization.Serializable

@Serializable
data class EServices  (
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String,
    val isVisible: Boolean,
    val imageUrl: String,
    val offered: Long = 0,
    val createdAt: String,
    val updatedAt: String,
    val price: Long,
    val categoryName: String,
    val categoryId: Long,
    val discount: Long = 0,
    val promotion: String = ""
)