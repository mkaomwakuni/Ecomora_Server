package est.ecomora.server.data.repository.sales

import est.ecomora.server.domain.model.sales.Sale

interface SalesDao {
    suspend fun insertSale(
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
    ): Sale?

    suspend fun getAllSales(): List<Sale>
    suspend fun getSaleById(id: Long): Sale?
    suspend fun getSalesByUserId(userId: Int): List<Sale>
    suspend fun getSalesByItemType(itemType: String): List<Sale>
    suspend fun getSalesByDateRange(startDate: String, endDate: String): List<Sale>
    suspend fun getTotalRevenue(): Long
    suspend fun getTotalSalesCount(): Int
}