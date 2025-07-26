package est.ecomora.server.data.repository.category

import est.ecomora.server.domain.model.category.Categories
import est.ecomora.server.domain.model.category.CategoryType


interface CategoriesDao {
    suspend fun insertCategory(
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String?,
        categoryType: CategoryType
    ): Categories?

    suspend fun getAllCategories(): List<Categories>?
    suspend fun getCategoryById(id: Long): Categories?
    suspend fun getCategoryByName(name: String): Categories?
    suspend fun deleteCategory(id: Long): Int?
    suspend fun updateCategory(
        id: Long,
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String?,
        categoryType: CategoryType
    ): Int?
}