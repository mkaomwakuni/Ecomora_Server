package est.ecomora.server.domain.repository.products


import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.repository.products.ProductDao
import est.ecomora.server.domain.model.products.Product
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class ProductsRepositoryImpl: ProductDao {
    override suspend fun insertProducts(
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
        sold: Long
    ): Product? {
      var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = ProductsTable.insert { item ->
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
                item[ProductsTable.sold] = sold
            }
        }
        return rowToResponse(arguments?.resultedValues?.get(0)!!)
    }

    override suspend fun getAllProduct(): List<Product>? {
        return DatabaseFactory.dbQuery {
            ProductsTable.selectAll()
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        return DatabaseFactory.dbQuery {
            ProductsTable.select { ProductsTable.id.eq(id) }
                .map {
                    rowToResponse(it)
                }.single()
        }
    }

    override suspend fun deleteProductById(id: Long): Int? {
        return DatabaseFactory.dbQuery {
            ProductsTable.deleteWhere { ProductsTable.id.eq(id) }
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
        promotion: String,
        productRating: Double,
        sold: Long
    ): Int? {
      return DatabaseFactory.dbQuery {
          ProductsTable.update({ ProductsTable.id.eq(id) }) {
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
              it[ProductsTable.sold] = sold
          }
      }
    }

    private fun rowToResponse(row: ResultRow): Product? {
        return if (row==null) {
            null
        } else {
            Product(
                id = row[ProductsTable.id],
                name = row[ProductsTable.name],
                description = row[ProductsTable.description],
                price = row[ProductsTable.price],
                imageUrl = row[ProductsTable.imageUrl],
                categoryName = row[ProductsTable.categoryName],
                categoryId = row[ProductsTable.categoryId],
                createdDate = row[ProductsTable.createdDate],
                updatedDate = row[ProductsTable.updatedDate],
                totalStock = row[ProductsTable.totalStock],
                brand = row[ProductsTable.brand],
                isAvailable = row[ProductsTable.isAvailable],
                discount = row[ProductsTable.discount],
                promotion = row[ProductsTable.promotion],
                productRating = row[ProductsTable.productRating],
                sold = row[ProductsTable.sold]
            )
        }
    }
}
