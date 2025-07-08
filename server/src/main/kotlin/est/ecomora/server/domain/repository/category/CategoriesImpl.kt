package est.ecomora.server.domain.repository.category


import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.repository.category.CategoriesDao
import est.ecomora.server.domain.model.category.Categories
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class CategoriesRepositoryImpl: CategoriesDao {
    override suspend fun insertCategory(
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String
    ): Categories? {
        var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = CategoriesTable.insert{ category ->
                category[CategoriesTable.name] = name
                category[CategoriesTable.description] = description
                category[CategoriesTable.productCount] = 0L // Initialize with 0 products
                category[CategoriesTable.isVisible] = isVisible
                category[CategoriesTable.imageUrl] = imageUrl
            }
        }
        return rowToResponse(arguments?.resultedValues?.get(0)!!)
    }

    override suspend fun getAllCategories(): List<Categories>? {
        return DatabaseFactory.dbQuery {
            CategoriesTable.selectAll()
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun getCategoryById(id: Long): Categories? {
        return DatabaseFactory.dbQuery {
            CategoriesTable.select(CategoriesTable.id.eq(id))
                .map {
                    rowToResponse(it)
                }.single()
        }
    }

    override suspend fun deleteCategory(id: Long): Int? {
        return DatabaseFactory.dbQuery {
            CategoriesTable.deleteWhere {
                CategoriesTable.id.eq(id)
            }
        }
    }

    override suspend fun updateCategory(
        id: Long,
        name: String,
        description: String,
        isVisible: Boolean,
        imageUrl: String
    ): Int? {
        return DatabaseFactory.dbQuery {
            CategoriesTable.update({ CategoriesTable.id.eq(id) }) { category ->
                category[CategoriesTable.id] = id
                category[CategoriesTable.name] = name
                category[CategoriesTable.description] = description
                category[CategoriesTable.isVisible] = isVisible
                category[CategoriesTable.imageUrl] = imageUrl
            }
        }
    }

    private fun rowToResponse(row: ResultRow): Categories? {
        return Categories(
            id = row[CategoriesTable.id],
            name = row[CategoriesTable.name],
            description = row[CategoriesTable.description],
            productCount = row[CategoriesTable.productCount],
            isVisible = row[CategoriesTable.isVisible],
            imageUrl = row[CategoriesTable.imageUrl]
        )
    }
}