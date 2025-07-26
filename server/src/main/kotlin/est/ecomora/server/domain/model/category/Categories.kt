package est.ecomora.server.domain.model.category

import kotlinx.serialization.Serializable

/**
 * Category type enum to distinguish between product and service categories
 */
enum class CategoryType(val displayName: String) {
    PRODUCT("Product"),
    SERVICE("Service")
}

@Serializable
data class Categories (
    val id: Long,
    val name: String,
    val description: String,
    val productCount: Long,
    val isVisible: Boolean,
    val imageUrl: String?,
    val categoryType: CategoryType
)