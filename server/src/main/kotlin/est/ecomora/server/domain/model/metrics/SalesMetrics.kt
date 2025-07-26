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

// New dashboard-specific models
@Serializable
data class DashboardMetrics(
    val dailyIncome: DailyIncomeData,
    val monthlyIncome: List<MonthlyIncomeData>,
    val topSellingItems: List<TopSellingItem>,
    val categorySales: List<CategorySalesData>,
    val recentOrders: List<RecentOrder>,
    val lowStockItems: List<LowStockItem>
)

@Serializable
data class DailyIncomeData(
    val date: String,
    val totalIncome: Long,
    val productIncome: Long,
    val serviceIncome: Long,
    val salesCount: Int,
    val productSalesCount: Int,
    val serviceSalesCount: Int
)

@Serializable
data class MonthlyIncomeData(
    val month: String,
    val totalIncome: Long,
    val productIncome: Long,
    val serviceIncome: Long
)

@Serializable
data class TopSellingItem(
    val id: Long,
    val name: String,
    val type: String, // "product", "service", "print"
    val totalSold: Long,
    val totalRevenue: Long,
    val category: String,
    val imageUrl: String?
)

@Serializable
data class CategorySalesData(
    val categoryName: String,
    val totalSales: Long,
    val totalRevenue: Long,
    val percentage: Double
)

@Serializable
data class RecentOrder(
    val id: Long,
    val productName: String,
    val date: String,
    val paymentType: String,
    val amount: Long,
    val status: String,
    val customerName: String?
)

@Serializable
data class IncomeChartData(
    val period: String, // "day", "week", "month"
    val data: List<IncomeDataPoint>
)

@Serializable
data class IncomeDataPoint(
    val label: String, // date or month label
    val income: Long
)

@Serializable
data class LowStockItem(
    val id: Long,
    val name: String,
    val currentStock: Long,
    val minStockLevel: Long,
    val category: String,
    val imageUrl: String?,
    val price: Long,
    val isAvailable: Boolean
)