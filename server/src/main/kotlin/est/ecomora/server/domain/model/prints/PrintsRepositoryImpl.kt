package est.ecomora.server.domain.model.prints

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.prints.PrintsTable
import est.ecomora.server.data.repository.prints.PrintsDao
import est.ecomora.server.domain.model.electronics.Prints
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class PrintsRepositoryImpl: PrintsDao {
    override suspend fun insertPrint(
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        copies: Int
    ): Prints? {
        return try {
            DatabaseFactory.dbQuery {
                val arguments = PrintsTable.insert { print ->
                    print[PrintsTable.name] = name
                    print[PrintsTable.description] = description
                    print[PrintsTable.price] = price
                    print[PrintsTable.imageUrl] = imageUrl
                    print[PrintsTable.copies] = copies
                }
                rowToResponse(arguments?.resultedValues?.get(0)!!)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllPrints(): List<Prints>? {
        return DatabaseFactory.dbQuery {
            PrintsTable.selectAll().mapNotNull { rowToResponse(it) }
        }
    }

    override suspend fun getPrintById(id: Long): Prints? {
        return DatabaseFactory.dbQuery {
            PrintsTable.select { PrintsTable.id eq id }
                .mapNotNull { rowToResponse(it) }
                .singleOrNull()
        }
    }

    override suspend fun updatePrint(
        id: Long,
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        copies: Int
    ): Int? {
        return DatabaseFactory.dbQuery {
            PrintsTable.update({ PrintsTable.id eq id }) { print ->
                print[PrintsTable.name] = name
                print[PrintsTable.description] = description
                print[PrintsTable.price] = price
                print[PrintsTable.imageUrl] = imageUrl
                print[PrintsTable.copies] = copies
            }
        }
    }

    private fun rowToResponse(row: ResultRow): Prints? {
        if (row == null) {
            return null
        } else {
            return Prints(
                id = row[PrintsTable.id],
                name = row[PrintsTable.name],
                description = row[PrintsTable.description],
                price = row[PrintsTable.price],
                imageUrl = row[PrintsTable.imageUrl],
                copies = row[PrintsTable.copies]
            )
        }
    }
}