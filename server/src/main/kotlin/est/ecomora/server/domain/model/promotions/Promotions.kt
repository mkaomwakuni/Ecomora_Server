package est.ecomora.server.domain.model.promotions

import kotlinx.serialization.Serializable

@Serializable
data class Promotions (
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val enabled: Boolean,
    val imageUrl: String,
    val startDate: Long,
    val endDate: Long
)