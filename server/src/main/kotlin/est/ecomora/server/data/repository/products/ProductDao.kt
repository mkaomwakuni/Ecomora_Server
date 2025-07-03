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
        isFeatured: Boolean
    ): Product?

    suspend fun getAllProduct(): List<Product>?
    suspend fun getProductById(id: Long): Product?
    suspend fun deleteProductById(id: Long): Int?
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
        promotion: String,
        productRating: Double,
        color: String,
        isFeatured: Boolean
    ): Int?
}
