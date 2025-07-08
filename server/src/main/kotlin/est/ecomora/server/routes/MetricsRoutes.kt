package est.ecomora.server.routes

import est.ecomora.server.domain.repository.sales.SalesRepositoryImpl
import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.domain.model.metrics.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

fun Route.metricsRoutes(
    salesDb: SalesRepositoryImpl,
    productsDb: ProductsRepositoryImpl,
    servicesDb: EservicesRepositoryImpl
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    
    route("v1/metrics") {
        // Get comprehensive sales metrics for dashboard
        get {
            try {
                val allSales = salesDb.getAllSales()
                val today = dateFormat.format(Date())
                val todaySales = allSales.filter { it.saleDate.startsWith(today) }
                
                // Calculate basic metrics
                val totalRevenue = allSales.sumOf { it.totalAmount }
                val totalSales = allSales.size
                val productSales = allSales.count { it.itemType == "product" }
                val serviceSales = allSales.count { it.itemType == "service" }
                val printSales = allSales.count { it.itemType == "print" }
                val todayRevenue = todaySales.sumOf { it.totalAmount }
                val todaySalesCount = todaySales.size
                
                // Calculate week and month revenue
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, -23) // Total 30 days ago
                val monthAgo = dateFormat.format(calendar.time)
                
                val weekSales = allSales.filter { it.saleDate >= weekAgo }
                val monthSales = allSales.filter { it.saleDate >= monthAgo }
                val weekRevenue = weekSales.sumOf { it.totalAmount }
                val monthRevenue = monthSales.sumOf { it.totalAmount }
                
                val averageSaleAmount = if (totalSales > 0) totalRevenue.toDouble() / totalSales else 0.0
                
                // Group sales by payment type
                val revenueByPaymentType = allSales.groupBy { it.paymentType }
                    .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                
                // Group sales by item type
                val revenueByItemType = allSales.groupBy { it.itemType }
                    .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                
                // Generate sales trend (last 7 days)
                val salesTrend = mutableListOf<DailySales>()
                for (i in 6 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val date = dateFormat.format(calendar.time)
                    val daySales = allSales.filter { it.saleDate.startsWith(date) }
                    salesTrend.add(
                        DailySales(
                            date = date,
                            totalSales = daySales.size,
                            totalRevenue = daySales.sumOf { it.totalAmount }
                        )
                    )
                }
                
                // Get top selling products
                val productSalesData = allSales.filter { it.itemType == "product" }
                    .groupBy { it.itemId }
                    .map { (itemId, sales) ->
                        ProductSalesData(
                            productId = itemId,
                            productName = sales.first().itemName,
                            totalSold = sales.sumOf { it.quantity }.toLong(),
                            totalRevenue = sales.sumOf { it.totalAmount },
                            remainingStock = 0L // Would need to query products table
                        )
                    }
                    .sortedByDescending { it.totalRevenue }
                    .take(10)
                
                // Get top selling services
                val serviceSalesData = allSales.filter { it.itemType == "service" }
                    .groupBy { it.itemId }
                    .map { (itemId, sales) ->
                        ServiceSalesData(
                            serviceId = itemId,
                            serviceName = sales.first().itemName,
                            totalOffered = sales.sumOf { it.quantity }.toLong(),
                            totalRevenue = sales.sumOf { it.totalAmount }
                        )
                    }
                    .sortedByDescending { it.totalRevenue }
                    .take(10)
                
                // Get top selling prints
                val printSalesData = allSales.filter { it.itemType == "print" }
                    .groupBy { it.itemId }
                    .map { (itemId, sales) ->
                        PrintSalesData(
                            printId = itemId,
                            printName = sales.first().itemName,
                            totalCopiesSold = sales.sumOf { it.quantity },
                            totalRevenue = sales.sumOf { it.totalAmount },
                            remainingCopies = 0 // Would need to query prints table
                        )
                    }
                    .sortedByDescending { it.totalRevenue }
                    .take(10)
                
                val salesMetrics = SalesMetrics(
                    totalRevenue = totalRevenue,
                    totalSales = totalSales,
                    productSales = productSales,
                    serviceSales = serviceSales,
                    printSales = printSales,
                    todayRevenue = todayRevenue,
                    todaySales = todaySalesCount,
                    weekRevenue = weekRevenue,
                    monthRevenue = monthRevenue,
                    averageSaleAmount = averageSaleAmount,
                    topSellingProducts = productSalesData,
                    topSellingServices = serviceSalesData,
                    topSellingPrints = printSalesData,
                    revenueByPaymentType = revenueByPaymentType,
                    revenueByItemType = revenueByItemType,
                    salesTrend = salesTrend
                )
                
                call.respond(HttpStatusCode.OK, salesMetrics)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Metrics: ${e.message}"
                )
            }
        }
        
        // Get revenue summary
        get("revenue") {
            try {
                val totalRevenue = salesDb.getTotalRevenue()
                val totalSales = salesDb.getTotalSalesCount()
                val averageRevenue = if (totalSales > 0) totalRevenue.toDouble() / totalSales else 0.0
                
                val summary = mapOf(
                    "totalRevenue" to totalRevenue,
                    "totalSales" to totalSales,
                    "averageRevenue" to averageRevenue
                )
                
                call.respond(HttpStatusCode.OK, summary)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Revenue: ${e.message}"
                )
            }
        }
        
        // Get sales by date range
        get("sales/{startDate}/{endDate}") {
            val startDate = call.parameters["startDate"]
                ?: return@get call.respondText(
                    text = "Start Date Missing",
                    status = HttpStatusCode.BadRequest
                )
            
            val endDate = call.parameters["endDate"]
                ?: return@get call.respondText(
                    text = "End Date Missing",
                    status = HttpStatusCode.BadRequest
                )
            
            try {
                val sales = salesDb.getSalesByDateRange(startDate, endDate)
                val revenue = sales.sumOf { it.totalAmount }
                val salesCount = sales.size
                
                val summary = mapOf(
                    "sales" to sales,
                    "totalRevenue" to revenue,
                    "totalSales" to salesCount,
                    "startDate" to startDate,
                    "endDate" to endDate
                )
                
                call.respond(HttpStatusCode.OK, summary)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Sales by Date Range: ${e.message}"
                )
            }
        }
    }
}