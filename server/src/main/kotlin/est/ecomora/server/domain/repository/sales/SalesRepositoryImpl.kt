package est.ecomora.server.domain.repository.sales


import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.local.table.sales.SalesTable
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.repository.sales.SalesDao
import est.ecomora.server.domain.model.sales.Sale
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update

class SalesRepositoryImpl : SalesDao {
    
    override suspend fun insertSale(
        userId: Int,
        itemId: Long,
        itemType: String,
        itemName: String,
        quantity: Int,
        unitPrice: Long,
        totalAmount: Long,
        paymentType: String,
        saleDate: String,
        timestamp: Long
    ): Sale? {
        var saleResult: InsertStatement<Number>? = null
        
        DatabaseFactory.dbQuery {
            when (itemType.lowercase()) {
                "product" -> {

                    val currentProduct = ProductsTable.select { ProductsTable.id eq itemId }.single()
                    val currentStock = currentProduct[ProductsTable.totalStock]
                    val currentSold = currentProduct[ProductsTable.sold]

                    ProductsTable.update({ ProductsTable.id eq itemId }) { product ->
                        product[totalStock] = currentStock - quantity.toLong()
                        product[sold] = currentSold + quantity.toLong()
                    }
                }
                "service" -> {

                    val currentService = EservicesTable.select { EservicesTable.id eq itemId }.single()
                    val currentOffered = currentService[EservicesTable.offered]
                    
                    // Update service offered count
                    EservicesTable.update({ EservicesTable.id eq itemId }) { service ->
                        service[offered] = currentOffered + quantity.toLong()
                    }
                }
            }

            saleResult = SalesTable.insert { sale ->
                sale[SalesTable.userId] = userId
                sale[SalesTable.itemId] = itemId
                sale[SalesTable.itemType] = itemType
                sale[SalesTable.itemName] = itemName
                sale[SalesTable.quantity] = quantity
                sale[SalesTable.unitPrice] = unitPrice
                sale[SalesTable.totalAmount] = totalAmount
                sale[SalesTable.paymentType] = paymentType
                sale[SalesTable.saleDate] = saleDate
                sale[SalesTable.timestamp] = timestamp
            }
        }
        
        return rowToSale(saleResult?.resultedValues?.get(0)!!)
    }

    override suspend fun getAllSales(): List<Sale> {
        return DatabaseFactory.dbQuery {
            SalesTable.selectAll().mapNotNull { rowToSale(it) }
        }
    }

    override suspend fun getSaleById(id: Long): Sale? {
        return DatabaseFactory.dbQuery {
            SalesTable.select { SalesTable.id eq id }
                .map { rowToSale(it) }
                .singleOrNull()
        }
    }

    override suspend fun getSalesByUserId(userId: Int): List<Sale> {
        return DatabaseFactory.dbQuery {
            SalesTable.select { SalesTable.userId eq userId }
                .mapNotNull { rowToSale(it) }
        }
    }

    override suspend fun getSalesByItemType(itemType: String): List<Sale> {
        return DatabaseFactory.dbQuery {
            SalesTable.select { SalesTable.itemType eq itemType }
                .mapNotNull { rowToSale(it) }
        }
    }

    override suspend fun getSalesByDateRange(startDate: String, endDate: String): List<Sale> {
        return DatabaseFactory.dbQuery {
            SalesTable.select { 
                (SalesTable.saleDate greaterEq startDate) and (SalesTable.saleDate lessEq endDate) 
            }.mapNotNull { rowToSale(it) }
        }
    }

    override suspend fun getTotalRevenue(): Long {
        return DatabaseFactory.dbQuery {
            SalesTable.slice(SalesTable.totalAmount.sum())
                .selectAll()
                .first()[SalesTable.totalAmount.sum()] ?: 0L
        }
    }

    override suspend fun getTotalSalesCount(): Int {
        return DatabaseFactory.dbQuery {
            SalesTable.selectAll().count().toInt()
        }
    }

    private fun rowToSale(row: ResultRow): Sale? {
        return if (row == null) {
            null
        } else {
            Sale(
                id = row[SalesTable.id],
                userId = row[SalesTable.userId],
                itemId = row[SalesTable.itemId],
                itemType = row[SalesTable.itemType],
                itemName = row[SalesTable.itemName],
                quantity = row[SalesTable.quantity],
                unitPrice = row[SalesTable.unitPrice],
                totalAmount = row[SalesTable.totalAmount],
                paymentType = row[SalesTable.paymentType],
                saleDate = row[SalesTable.saleDate],
                timestamp = row[SalesTable.timestamp]
            )
        }
    }
}