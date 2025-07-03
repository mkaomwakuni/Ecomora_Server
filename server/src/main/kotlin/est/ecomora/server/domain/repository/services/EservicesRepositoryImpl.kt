package est.ecomora.server.domain.repository.services

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.repository.services.EservicesDao
import est.ecomora.server.domain.model.services.EServices
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
        category: String,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String
    ): EServices? {
        var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = EservicesTable.insert { service ->
                service[EservicesTable.name] = name
                service[EservicesTable.description] = description
                service[EservicesTable.price] = price
                service[EservicesTable.offered] = offered
                service[EservicesTable.category] = category
                service[EservicesTable.imageUrl] = imageUrl
                service[EservicesTable.isVisible] = isVisible
                service[EservicesTable.createdAt] = createdAt
                service[EservicesTable.updatedAt] = updatedAt
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
        category: String,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String
    ): Int =
        DatabaseFactory.dbQuery {
                 EservicesTable.update({ EservicesTable.id.eq(id) }) { service ->
                service[EservicesTable.name] = name
                service[EservicesTable.description] = description
                service[EservicesTable.price] = price
                service[EservicesTable.offered] = offered
                service[EservicesTable.category] = category
                service[EservicesTable.imageUrl] = imageUrl
                service[EservicesTable.isVisible] = isVisible
                service[EservicesTable.createdAt] = createdAt
                service[EservicesTable.updatedAt] = updatedAt
            }
    }

    override suspend fun getServiceById(id: Long): EServices? =
        DatabaseFactory.dbQuery {
            EservicesTable.select {
                EservicesTable.id.eq(id)
            }.map {
                rowToResponse(it)
            }.singleOrNull()
        }

    override suspend fun getAllServices(): List<EServices>? =
        DatabaseFactory.dbQuery {
            EservicesTable.selectAll().mapNotNull {
                rowToResponse(it)
            }
    }

    override suspend fun deleteServiceById(id: Long): Int  =
        DatabaseFactory.dbQuery {
            EservicesTable.deleteWhere {
                EservicesTable.id.eq(id)
            }
        }
    }


    private fun rowToResponse(row: ResultRow): EServices? {
        if (row == null) {
            return null
        } else {
            return EServices(
                id = row[EservicesTable.id],
                name = row[EservicesTable.name],
                description = row[EservicesTable.description],
                price = row[EservicesTable.price],
                offered = row[EservicesTable.offered],
                category = row[EservicesTable.category],
                imageUrl = row[EservicesTable.imageUrl],
                isVisible = row[EservicesTable.isVisible],
                createdAt = row[EservicesTable.createdAt],
                updatedAt = row[EservicesTable.updatedAt]
            )
        }
    }