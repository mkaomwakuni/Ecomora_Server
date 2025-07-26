package est.ecomora.server.routes

import est.ecomora.server.domain.repository.sales.SalesRepositoryImpl
import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.domain.model.metrics.*
import est.ecomora.server.plugins.getCurrentUserId
import io.ktor.http.*
import io.ktor.server.auth.*
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
        authenticate("auth-jwt") {

            get {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    "User not authenticated"
                )

                try {
                    val allSales = salesDb.getSalesByUserId(userId.toInt())
                    val today = dateFormat.format(Date())
                    val todaySales = allSales.filter { it.saleDate.startsWith(today) }

                    // Calculate basic metrics
                    val totalRevenue = allSales.sumOf { it.totalAmount }
                    val totalSales = allSales.size
                    val productSales = allSales.count { it.itemType == "product" }
                    val serviceSales = allSales.count { it.itemType == "service" }
                    val todayRevenue = todaySales.sumOf { it.totalAmount }
                    val todaySalesCount = todaySales.size

                    // Calculate week and month revenue
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    val weekAgo = dateFormat.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, -23)
                    val monthAgo = dateFormat.format(calendar.time)

                    val weekSales = allSales.filter { it.saleDate >= weekAgo }
                    val monthSales = allSales.filter { it.saleDate >= monthAgo }
                    val weekRevenue = weekSales.sumOf { it.totalAmount }
                    val monthRevenue = monthSales.sumOf { it.totalAmount }

                    val averageSaleAmount =
                        if (totalSales > 0) totalRevenue.toDouble() / totalSales else 0.0

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


                    val productSalesData = allSales.filter { it.itemType == "product" }
                        .groupBy { it.itemId }
                        .map { (itemId, sales) ->
                            ProductSalesData(
                                productId = itemId,
                                productName = sales.first().itemName,
                                totalSold = sales.sumOf { it.quantity }.toLong(),
                                totalRevenue = sales.sumOf { it.totalAmount },
                                remainingStock = 0L
                            )
                        }
                        .sortedByDescending { it.totalRevenue }
                        .take(10)

                    // Get top selling services (user-specific)
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

                    val salesMetrics = SalesMetrics(
                        totalRevenue = totalRevenue,
                        totalSales = totalSales,
                        productSales = productSales,
                        serviceSales = serviceSales,
                        printSales = 0,
                        todayRevenue = todayRevenue,
                        todaySales = todaySalesCount,
                        weekRevenue = weekRevenue,
                        monthRevenue = monthRevenue,
                        averageSaleAmount = averageSaleAmount,
                        topSellingProducts = productSalesData,
                        topSellingServices = serviceSalesData,
                        topSellingPrints = emptyList(),
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

            // Get comprehensive dashboard metrics
            get("dashboard") {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    "User not authenticated"
                )

                try {
                    val allSales = salesDb.getSalesByUserId(userId.toInt())
                    val today = dateFormat.format(Date())
                    val todaySales = allSales.filter { it.saleDate.startsWith(today) }

                    // Daily income data
                    val dailyIncome = DailyIncomeData(
                        date = today,
                        totalIncome = todaySales.sumOf { it.totalAmount },
                        productIncome = todaySales.filter { it.itemType == "product" }
                            .sumOf { it.totalAmount },
                        serviceIncome = todaySales.filter { it.itemType == "service" }
                            .sumOf { it.totalAmount },
                        salesCount = todaySales.size,
                        productSalesCount = todaySales.count { it.itemType == "product" },
                        serviceSalesCount = todaySales.count { it.itemType == "service" }
                    )

                    // Monthly income data (last 12 months)
                    val monthlyIncome = mutableListOf<MonthlyIncomeData>()
                    val calendar = Calendar.getInstance()

                    for (i in 11 downTo 0) {
                        calendar.time = Date()
                        calendar.add(Calendar.MONTH, -i)
                        val monthStart = SimpleDateFormat("yyyy-MM-01").format(calendar.time)
                        calendar.add(Calendar.MONTH, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val monthEnd = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

                        val monthSales =
                            allSales.filter { it.saleDate >= monthStart && it.saleDate <= monthEnd }
                        val monthName = SimpleDateFormat("MMM yyyy").format(calendar.time)

                        monthlyIncome.add(
                            MonthlyIncomeData(
                                month = monthName,
                                totalIncome = monthSales.sumOf { it.totalAmount },
                                productIncome = monthSales.filter { it.itemType == "product" }
                                    .sumOf { it.totalAmount },
                                serviceIncome = monthSales.filter { it.itemType == "service" }
                                    .sumOf { it.totalAmount }
                            )
                        )
                    }


                    val topSellingItems = mutableListOf<TopSellingItem>()

                    // Get product sales
                    val productSales = allSales.filter { it.itemType == "product" }
                        .groupBy { it.itemId }
                        .map { (itemId, sales) ->
                            val product = productsDb.getProductById(itemId, userId)
                            TopSellingItem(
                                id = itemId,
                                name = sales.first().itemName,
                                type = "product",
                                totalSold = sales.sumOf { it.quantity }.toLong(),
                                totalRevenue = sales.sumOf { it.totalAmount },
                                category = product?.categoryName ?: "Unknown",
                                imageUrl = product?.imageUrl
                            )
                        }

                    // Get service sales
                    val serviceSales = allSales.filter { it.itemType == "service" }
                        .groupBy { it.itemId }
                        .map { (itemId, sales) ->
                            val service = servicesDb.getServiceById(itemId, userId)
                            TopSellingItem(
                                id = itemId,
                                name = sales.first().itemName,
                                type = "service",
                                totalSold = sales.sumOf { it.quantity }.toLong(),
                                totalRevenue = sales.sumOf { it.totalAmount },
                                category = service?.categoryName ?: "Services",
                                imageUrl = service?.imageUrl
                            )
                        }


                    topSellingItems.addAll(productSales)
                    topSellingItems.addAll(serviceSales)
                    val top4Items = topSellingItems.sortedByDescending { it.totalRevenue }.take(4)

                    val categorySales = mutableListOf<CategorySalesData>()
                    val totalRevenue = allSales.sumOf { it.totalAmount }

                    // Group by category
                    val categoryMap =
                        mutableMapOf<String, Pair<Long, Long>>()

                    for (sale in allSales) {
                        val category = when (sale.itemType) {
                            "product" -> {
                                val product = productsDb.getProductById(sale.itemId, userId)
                                product?.categoryName ?: "Unknown"
                            }

                            "service" -> {
                                val service = servicesDb.getServiceById(sale.itemId, userId)
                                service?.categoryName ?: "Services"
                            }

                            else -> "Other"
                        }

                        val current = categoryMap[category] ?: Pair(0L, 0L)
                        categoryMap[category] = Pair(
                            current.first + sale.quantity,
                            current.second + sale.totalAmount
                        )
                    }

                    for ((category, data) in categoryMap) {
                        val percentage =
                            if (totalRevenue > 0) (data.second.toDouble() / totalRevenue * 100) else 0.0
                        categorySales.add(
                            CategorySalesData(
                                categoryName = category,
                                totalSales = data.first,
                                totalRevenue = data.second,
                                percentage = percentage
                            )
                        )
                    }


                    val recentOrders = allSales.sortedByDescending { it.timestamp }
                        .take(10)
                        .map { sale ->
                            RecentOrder(
                                id = sale.id,
                                productName = sale.itemName,
                                date = sale.saleDate,
                                paymentType = sale.paymentType,
                                amount = sale.totalAmount,
                                status = "Complete",
                                customerName = null
                            )
                        }

                    // Low stock items (user-specific products)
                    val lowStockItems = mutableListOf<LowStockItem>()
                    val allProducts = productsDb.getAllProductsByUserId(userId)

                    allProducts?.forEach { product ->
                        val minStockLevel = 10L
                        if (product.totalStock <= minStockLevel && product.isAvailable) {
                            lowStockItems.add(
                                LowStockItem(
                                    id = product.id,
                                    name = product.name,
                                    currentStock = product.totalStock,
                                    minStockLevel = minStockLevel,
                                    category = product.categoryName,
                                    imageUrl = product.imageUrl,
                                    price = product.price,
                                    isAvailable = product.isAvailable
                                )
                            )
                        }
                    }

                    val dashboardMetrics = DashboardMetrics(
                        dailyIncome = dailyIncome,
                        monthlyIncome = monthlyIncome,
                        topSellingItems = top4Items,
                        categorySales = categorySales,
                        recentOrders = recentOrders,
                        lowStockItems = lowStockItems
                    )

                    call.respond(HttpStatusCode.OK, dashboardMetrics)
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        "Error While Fetching Dashboard Metrics: ${e.message}"
                    )
                }
            }

            // Get low stock items (user-specific)
            get("low-stock") {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    "User not authenticated"
                )

                try {
                    val lowStockItems = mutableListOf<LowStockItem>()
                    val allProducts = productsDb.getAllProductsByUserId(userId)
                    val minStockLevel =
                        call.request.queryParameters["threshold"]?.toLongOrNull() ?: 10L

                    allProducts?.forEach { product ->
                        if (product.totalStock <= minStockLevel && product.isAvailable) {
                            lowStockItems.add(
                                LowStockItem(
                                    id = product.id,
                                    name = product.name,
                                    currentStock = product.totalStock,
                                    minStockLevel = minStockLevel,
                                    category = product.categoryName,
                                    imageUrl = product.imageUrl,
                                    price = product.price,
                                    isAvailable = product.isAvailable
                                )
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, lowStockItems)
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        "Error While Fetching Low Stock Items: ${e.message}"
                    )
                }
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

        // Get income chart data by period
        get("income/{period}") {
            val period = call.parameters["period"] ?: "day"
            
            try {
                val allSales = salesDb.getAllSales()
                val data = mutableListOf<IncomeDataPoint>()
                
                when (period) {
                    "day" -> {
                        // Last 7 days
                        for (i in 6 downTo 0) {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, -i)
                            val date = dateFormat.format(calendar.time)
                            val daySales = allSales.filter { it.saleDate.startsWith(date) }
                            data.add(
                                IncomeDataPoint(
                                    label = SimpleDateFormat("MMM dd").format(calendar.time),
                                    income = daySales.sumOf { it.totalAmount }
                                )
                            )
                        }
                    }
                    "week" -> {
                        // Last 8 weeks
                        for (i in 7 downTo 0) {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.WEEK_OF_YEAR, -i)
                            val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }.time
                            val weekEnd = calendar.apply { add(Calendar.DAY_OF_WEEK, 6) }.time
                            
                            val weekSales = allSales.filter { 
                                val saleDate = dateFormat.parse(it.saleDate)
                                saleDate >= weekStart && saleDate <= weekEnd
                            }
                            
                            data.add(
                                IncomeDataPoint(
                                    label = "Week ${SimpleDateFormat("w").format(weekStart)}",
                                    income = weekSales.sumOf { it.totalAmount }
                                )
                            )
                        }
                    }
                    "month" -> {
                        // Last 12 months
                        for (i in 11 downTo 0) {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MONTH, -i)
                            val monthStart = SimpleDateFormat("yyyy-MM-01").format(calendar.time)
                            calendar.add(Calendar.MONTH, 1)
                            calendar.add(Calendar.DAY_OF_MONTH, -1)
                            val monthEnd = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
                            
                            val monthSales = allSales.filter { it.saleDate >= monthStart && it.saleDate <= monthEnd }
                            
                            data.add(
                                IncomeDataPoint(
                                    label = SimpleDateFormat("MMM yyyy").format(calendar.time),
                                    income = monthSales.sumOf { it.totalAmount }
                                )
                            )
                        }
                    }
                }
                
                val chartData = IncomeChartData(
                    period = period,
                    data = data
                )
                
                call.respond(HttpStatusCode.OK, chartData)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Income Chart Data: ${e.message}"
                )
            }
        }
    }
}