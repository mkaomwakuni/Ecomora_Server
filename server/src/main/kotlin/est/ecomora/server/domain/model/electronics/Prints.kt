package est.ecomora.server.domain.model.electronics

import kotlinx.serialization.Serializable

@Serializable
data class Prints (
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val copies: Int
)