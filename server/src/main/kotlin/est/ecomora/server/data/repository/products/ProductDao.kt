package est.ecomora.server.data.repository.products

import est.ecomora.server.domain.model.products.Product


interface ProductDao {
    suspend fun insertProduct (
        name: String,
        description: String,
        price: Long,
        imageUrl: String,
        categoryName: String,
        categoryId: Long,
        createdDate: String,
        updatedDate: String,
        totalStock: Long,
        brand: String,
        isAvailable: Boolean,
        discount: Long,
        promotion: String,
        productRating: Double,
        color: String,
        sold: Long,
        isFeatured: Boolean,
        userId: Long
    ): Product?

    suspend fun getAllProductsByUserId(userId: Long): List<Product>?
    suspend fun getProductsByIds(ids: List<Long>, userId: Long): List<Product>?
    suspend fun getProductById(id: Long, userId: Long): Product?
    suspend fun deleteProductById(id: Long, userId: Long): Int?

    suspend fun getProductsByMultipleIds(ids: List<Long>, userId: Long): List<Product>?

    suspend fun updateProductById(
        id: Long,
        name: String,
        description: String,
        price: Long,
        imageUrl: String,
        categoryName: String,
        categoryId: Long,
        createdDate: String,
        updatedDate: String,
        totalStock: Long,
        brand: String,
        isAvailable: Boolean,
        discount: Long,
        sold: Long,
        promotion: String,
        productRating: Double,
        color: String,
        isFeatured: Boolean,
        userId: Long
    ): Int?

    suspend fun updateSoldCounter(productId: Long, quantity: Long, userId: Long): Int?
}
