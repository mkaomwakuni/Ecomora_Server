package est.tunzo.cyberpros.server.domain.model.category

import kotlinx.serialization.Serializable

@Serializable
data class Categories (
    val id: Long,
    val name: String,
    val description: String,
    val isVisible: Boolean,
    val imageUrl: String
)