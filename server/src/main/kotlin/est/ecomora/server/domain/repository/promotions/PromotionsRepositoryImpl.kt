package est.ecomora.server.domain.repository.promotions

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.promotions.PromotionTable
import est.ecomora.server.data.repository.promotions.PromotionDao
import est.ecomora.server.domain.model.promotions.Promotions
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class PromotionsRepositoryImpl: PromotionDao {
    override suspend fun insertPromo(
        userId: Long,
        title: String,
        description: String,
        imageUrl: String,
        startDate: Long,
        endDate: Long,
        enabled: Boolean
    ): Promotions? {
        var arguments: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            arguments = PromotionTable.insert { promotion ->
                promotion[PromotionTable.userId] = userId
                promotion[PromotionTable.title] = title
                promotion[PromotionTable.description] = description
                promotion[PromotionTable.imageUrl] = imageUrl
                promotion[PromotionTable.startDate] = startDate
                promotion[PromotionTable.endDate] = endDate
                promotion[PromotionTable.enabled] = enabled
            }
        }
        return rowToResponse(arguments?.resultedValues?.get(0)!!)
    }

    override suspend fun updatePromo(
        id: Long,
        userId: Long,
        title: String,
        description: String,
        imageUrl: String,
        startDate: Long,
        endDate: Long,
        enabled: Boolean
    ): Promotions? {
        DatabaseFactory.dbQuery {
            PromotionTable.update({ PromotionTable.id eq id }) {
                it[PromotionTable.userId] = userId
                it[PromotionTable.title] = title
                it[PromotionTable.description] = description
                it[PromotionTable.imageUrl] = imageUrl
                it[PromotionTable.startDate] = startDate
                it[PromotionTable.endDate] = endDate
                it[PromotionTable.enabled] = enabled
            }
        }
        return getPromotionById(id)
    }

    override suspend fun getAllPromotions(): List<Promotions>? {
        return DatabaseFactory.dbQuery {
            PromotionTable.selectAll()
                .mapNotNull {
                    rowToResponse(it)
                }
        }
    }

    override suspend fun getPromotionById(id: Long): Promotions? {
        return DatabaseFactory.dbQuery {
            PromotionTable.select { PromotionTable.id.eq(id) }
                .map {
                    rowToResponse(it)
                }.single()
        }
    }

    override suspend fun deletePromotionById(id: Long): Int {
        return DatabaseFactory.dbQuery {
            PromotionTable.deleteWhere {  PromotionTable.id.eq(id) }
        }
    }

    private fun rowToResponse(row: ResultRow): Promotions? {
        if (row == null) {
            return null
        } else {
            return Promotions(
                id = row[PromotionTable.id],
                title = row[PromotionTable.title],
                description = row[PromotionTable.description],
                imageUrl = row[PromotionTable.imageUrl],
                startDate = row[PromotionTable.startDate],
                endDate = row[PromotionTable.endDate],
                enabled = row[PromotionTable.enabled],
                userId = row[PromotionTable.userId]
            )
        }
    }
}