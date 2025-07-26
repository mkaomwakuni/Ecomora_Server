package est.ecomora.server.domain.repository.products

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.repository.products.ProductDao
import est.ecomora.server.domain.model.products.Product
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ProductsRepositoryImpl: ProductDao {
    override suspend fun insertProduct(
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
    ): Product? {
        return try {
            transaction {
                val arguments = ProductsTable.insert { item ->
                    item[ProductsTable.userId] = userId
                    item[ProductsTable.name] = name
                    item[ProductsTable.description] = description
                    item[ProductsTable.price] = price
                    item[ProductsTable.imageUrl] = imageUrl
                    item[ProductsTable.categoryName] = categoryName
                    item[ProductsTable.categoryId] = categoryId
                    item[ProductsTable.createdDate] = createdDate
                    item[ProductsTable.updatedDate] = updatedDate
                    item[ProductsTable.totalStock] = totalStock
                    item[ProductsTable.brand] = brand
                    item[ProductsTable.isAvailable] = isAvailable
                    item[ProductsTable.discount] = discount
                    item[ProductsTable.promotion] = promotion
                    item[ProductsTable.productRating] = productRating
                    item[ProductsTable.color] = color
                    item[ProductsTable.sold] = sold
                    item[ProductsTable.isFeatured] = isFeatured
                }
                val firstResponse = arguments.resultedValues?.firstOrNull()!!
                rowToResponse(firstResponse)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllProductsByUserId(userId: Long): List<Product>? {
        return DatabaseFactory.dbQuery {
            ProductsTable.select { ProductsTable.userId.eq(userId) }
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun getProductsByIds(ids: List<Long>, userId: Long): List<Product>? {
        return DatabaseFactory.dbQuery {
            ProductsTable.select { ProductsTable.id.inList(ids) and ProductsTable.userId.eq(userId) }
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun getProductById(id: Long, userId: Long): Product? {
        return DatabaseFactory.dbQuery {
            ProductsTable.select { ProductsTable.id.eq(id) and ProductsTable.userId.eq(userId) }
                .map {
                    rowToResponse(it)
                }.singleOrNull()
        }
    }

    override suspend fun deleteProductById(id: Long, userId: Long): Int? {
        return DatabaseFactory.dbQuery {
            ProductsTable.deleteWhere { ProductsTable.id.eq(id) and ProductsTable.userId.eq(userId) }
        }
    }

    override suspend fun getProductsByMultipleIds(ids: List<Long>, userId: Long): List<Product>? {
        return DatabaseFactory.dbQuery {
            ProductsTable.select { ProductsTable.id.inList(ids) and ProductsTable.userId.eq(userId) }
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun updateProductById(
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
    ): Int? {
        return DatabaseFactory.dbQuery {
            ProductsTable.update({ ProductsTable.id.eq(id) and ProductsTable.userId.eq(userId) }) {
                it[ProductsTable.name] = name
                it[ProductsTable.description] = description
                it[ProductsTable.price] = price
                it[ProductsTable.imageUrl] = imageUrl
                it[ProductsTable.categoryName] = categoryName
                it[ProductsTable.categoryId] = categoryId
                it[ProductsTable.createdDate] = createdDate
                it[ProductsTable.updatedDate] = updatedDate
                it[ProductsTable.totalStock] = totalStock
                it[ProductsTable.brand] = brand
                it[ProductsTable.isAvailable] = isAvailable
                it[ProductsTable.discount] = discount
                it[ProductsTable.promotion] = promotion
                it[ProductsTable.productRating] = productRating
                it[ProductsTable.color] = color
                it[ProductsTable.sold] = sold
                it[ProductsTable.isFeatured] = isFeatured
            }
        }
    }

    override suspend fun updateSoldCounter(productId: Long, quantity: Long, userId: Long): Int? {
        return DatabaseFactory.dbQuery {
            ProductsTable.update({ ProductsTable.id.eq(productId) and ProductsTable.userId.eq(userId) }) {
                it[ProductsTable.sold] = ProductsTable.sold.plus(quantity)
            }
        }
    }

    private fun rowToResponse(row: ResultRow): Product? {
        return if (row==null) {
            null
        } else {
            val categoryName = CategoriesTable.select { CategoriesTable.id eq row[ProductsTable.categoryId] }
                .map { it[CategoriesTable.name] }
                .singleOrNull()?: ""
            Product(
                id = row[ProductsTable.id],
                name = row[ProductsTable.name],
                description = row[ProductsTable.description],
                price = row[ProductsTable.price],
                imageUrl = row[ProductsTable.imageUrl],
                categoryId = row[ProductsTable.categoryId],
                categoryName = categoryName,
                createdDate = row[ProductsTable.createdDate],
                updatedDate = row[ProductsTable.updatedDate],
                totalStock = row[ProductsTable.totalStock],
                brand = row[ProductsTable.brand],
                isAvailable = row[ProductsTable.isAvailable],
                discount = row[ProductsTable.discount],
                promotion = row[ProductsTable.promotion],
                productRating = row[ProductsTable.productRating],
                sold = row[ProductsTable.sold],
                isFeatured = row[ProductsTable.isFeatured],
                color = row[ProductsTable.color],
                userId = row[ProductsTable.userId],
            )
        }
    }
}
