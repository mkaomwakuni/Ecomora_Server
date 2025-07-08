package est.ecomora.server.domain.model.metrics

import kotlinx.serialization.Serializable

@Serializable
data class SalesMetrics(
    val totalRevenue: Long,
    val totalSales: Int,
    val productSales: Int,
    val serviceSales: Int,
    val printSales: Int,
    val todayRevenue: Long,
    val todaySales: Int,
    val weekRevenue: Long,
    val monthRevenue: Long,
    val averageSaleAmount: Double,
    val topSellingProducts: List<ProductSalesData>,
    val topSellingServices: List<ServiceSalesData>,
    val topSellingPrints: List<PrintSalesData>,
    val revenueByPaymentType: Map<String, Long>,
    val revenueByItemType: Map<String, Long>,
    val salesTrend: List<DailySales>
)

@Serializable
data class ProductSalesData(
    val productId: Long,
    val productName: String,
    val totalSold: Long,
    val totalRevenue: Long,
    val remainingStock: Long
)

@Serializable
data class ServiceSalesData(
    val serviceId: Long,
    val serviceName: String,
    val totalOffered: Long,
    val totalRevenue: Long
)

@Serializable
data class PrintSalesData(
    val printId: Long,
    val printName: String,
    val totalCopiesSold: Int,
    val totalRevenue: Long,
    val remainingCopies: Int
)

@Serializable
data class DailySales(
    val date: String,
    val totalSales: Int,
    val totalRevenue: Long
)