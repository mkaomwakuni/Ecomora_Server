package est.tunzo.cyberpros.server.domain.model.services

import kotlinx.serialization.Serializable

@Serializable
data class Services(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val offered: Int
)
