package est.ecomora.server.domain.repository.services

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.repository.services.EservicesDao
import est.ecomora.server.domain.model.services.EServices
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class EservicesRepositoryImpl: EservicesDao {
    override suspend fun insertService(
        name: String,
        description: String,
        price: Long,
        offered: Long,
        categoryName: String,
        categoryId: Long,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String,
        userId: Long,
        discount: Long,
        promotion: String
    ): EServices? {
        var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = EservicesTable.insert { service ->
                service[EservicesTable.userId] = userId
                service[EservicesTable.name] = name
                service[EservicesTable.description] = description
                service[EservicesTable.price] = price
                service[EservicesTable.offered] = offered
                service[EservicesTable.sold] = 0
                service[EservicesTable.categoryName] = categoryName
                service[EservicesTable.categoryId] = categoryId
                service[EservicesTable.imageUrl] = imageUrl
                service[EservicesTable.isVisible] = isVisible
                service[EservicesTable.createdAt] = createdAt
                service[EservicesTable.updatedAt] = updatedAt
                service[EservicesTable.discount] = discount
                service[EservicesTable.promotion] = promotion
            }
        }
        return rowToResponse(arguments?.resultedValues?.get(0)!!)
    }

    override suspend fun updateService(
        id: Long,
        name: String,
        description: String,
        price: Long,
        offered: Long,
        categoryName: String,
        categoryId: Long,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String,
        userId: Long,
        discount: Long,
        promotion: String
    ): Int =
        DatabaseFactory.dbQuery {
            EservicesTable.update({ EservicesTable.id.eq(id) and EservicesTable.userId.eq(userId) }) { service ->
                service[EservicesTable.name] = name
                service[EservicesTable.description] = description
                service[EservicesTable.price] = price
                service[EservicesTable.offered] = offered
                service[EservicesTable.categoryName] = categoryName
                service[EservicesTable.categoryId] = categoryId
                service[EservicesTable.imageUrl] = imageUrl
                service[EservicesTable.isVisible] = isVisible
                service[EservicesTable.createdAt] = createdAt
                service[EservicesTable.updatedAt] = updatedAt
                service[EservicesTable.discount] = discount
                service[EservicesTable.promotion] = promotion
            }
    }

    override suspend fun getServiceById(id: Long, userId: Long): EServices? =
        DatabaseFactory.dbQuery {
            EservicesTable.select {
                EservicesTable.id.eq(id) and EservicesTable.userId.eq(userId)
            }.map {
                rowToResponse(it)
            }.singleOrNull()
        }

    override suspend fun getAllServicesByUserId(userId: Long): List<EServices>? =
        DatabaseFactory.dbQuery {
            EservicesTable.select { EservicesTable.userId.eq(userId) }.mapNotNull {
                rowToResponse(it)
            }
        }

    override suspend fun deleteServiceById(id: Long, userId: Long): Int =
        DatabaseFactory.dbQuery {
            EservicesTable.deleteWhere {
                EservicesTable.id.eq(id) and EservicesTable.userId.eq(userId)
            }
        }

    override suspend fun updateOfferedCounter(serviceId: Long, quantity: Long, userId: Long): Int? {
        return DatabaseFactory.dbQuery {
            EservicesTable.update({
                EservicesTable.id.eq(serviceId) and EservicesTable.userId.eq(
                    userId
                )
            }) {
                it[EservicesTable.offered] = EservicesTable.offered.plus(quantity)
            }
        }
    }

    suspend fun updateSoldCounter(serviceId: Long, quantity: Long, userId: Long): Int? {
        return DatabaseFactory.dbQuery {
            EservicesTable.update({
                EservicesTable.id.eq(serviceId) and EservicesTable.userId.eq(userId)
            }) {
                it[EservicesTable.sold] = EservicesTable.sold.plus(quantity)
            }
        }
    }

    private fun rowToResponse(row: ResultRow): EServices? {
        if (row == null) {
            return null
        } else {
            return EServices(
                id = row[EservicesTable.id],
                userId = row[EservicesTable.userId],
                name = row[EservicesTable.name],
                description = row[EservicesTable.description],
                price = row[EservicesTable.price],
                offered = row[EservicesTable.offered],
                categoryName = row[EservicesTable.categoryName],
                categoryId = row[EservicesTable.categoryId],
                imageUrl = row[EservicesTable.imageUrl],
                isVisible = row[EservicesTable.isVisible],
                createdAt = row[EservicesTable.createdAt],
                updatedAt = row[EservicesTable.updatedAt],
                discount = row[EservicesTable.discount],
                promotion = row[EservicesTable.promotion]
            )
        }
    }
}