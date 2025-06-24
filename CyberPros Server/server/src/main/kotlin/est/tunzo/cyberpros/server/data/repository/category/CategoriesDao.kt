package est.tunzo.cyberpros.server.data.repository.category

import est.tunzo.cyberpros.server.domain.model.category.Categories

interface CategoriesDao {
    suspend fun insertCategory(
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String,
    ): Categories?

    suspend fun getAllCategories(): List<Categories>?
    suspend fun getCategoryById(id: Long): Categories?
    suspend fun deleteCategory(id: Long): Int?
    suspend fun updateCategory(
        id: Long,
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String
    ): Int?
}