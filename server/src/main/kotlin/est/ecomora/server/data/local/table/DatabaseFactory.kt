package est.ecomora.server.data.local.table

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import est.ecomora.server.*
import est.ecomora.server.data.local.table.cart.CartTable
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.local.table.order.OrderTable
import est.ecomora.server.data.local.table.prints.PrintsTable
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.local.table.promotions.PromotionTable
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.local.table.users.UsersTable
import est.ecomora.server.domain.service.PasswordService
import est.ecomora.server.plugins.AppLogger
import est.ecomora.server.plugins.DatabaseLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Object responsible for setting up and managing the database connection using HikariCP and Exposed ORM.
 */
object DatabaseFactory {

    private var dataSource: HikariDataSource? = null
    private var isInitialized = false

    /**
     * Initializes the database connection.
     * - Connects to the database using the Hikari connection pool.
     * - Creates tables if they don't exist.
     * - Inserts test data for development.
     */
    fun init() {
        if (isInitialized) {
            AppLogger.info("Database already initialized, skipping...")
            return
        }

        try {
            // Create single HikariCP DataSource
            dataSource = hikariDataSource()

            // Establish a single connection to the database
            Database.connect(dataSource!!)
            DatabaseLogger.logConnection("Connected to database successfully")

            // Create tables and insert test data
            transaction {
                // Create all tables
                SchemaUtils.create(
                    UsersTable,
                    CategoriesTable,
                    PromotionTable,
                    EservicesTable,
                    ProductsTable,
                    PrintsTable,
                    CartTable,
                    OrderTable
                )

                // Migration: Update password column if needed
                try {
                    exec("ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(500);")
                    AppLogger.info("Password column migration completed")
                } catch (e: Exception) {
                    AppLogger.debug("Password column migration not needed: ${e.message}")
                }

                // Insert test data only in development mode
                if (!IS_PRODUCTION) {
                    insertTestData()
                } else {
                    AppLogger.info("Production mode: Skipping test data insertion")
                }
            }

            isInitialized = true
            AppLogger.info("Database initialization completed successfully")

        } catch (e: Exception) {
            AppLogger.error("Database initialization failed", e)
            throw e
        }
    }

    /**
     * Inserts comprehensive test data for all endpoints (Development only)
     */
    fun insertTestData() {
        try {
            transaction {
                AppLogger.info("Inserting comprehensive test data...")

                // Insert test users with proper password hashing
                val testUsers = listOf(
                    Triple("admin@example.com", "admin123", "admin"),
                    Triple("testuser1@example.com", "password123", "user"),
                    Triple("testuser2@example.com", "password123", "user")
                )

                val userIds = mutableMapOf<String, Long>()
                testUsers.forEach { (email, password, role) ->
                    if (UsersTable.select { UsersTable.email eq email }.empty()) {
                        val userId = UsersTable.insert {
                            it[username] = email.substringBefore("@")
                            it[UsersTable.password] = PasswordService.hashPassword(password)
                            it[UsersTable.email] = email
                            it[phoneNumber] = "+1234567890"
                            it[userRole] = role
                            it[userImage] = "/images/default-user.png"
                            it[fullName] = if (role == "admin") "System Admin" else "Test User"
                        } get UsersTable.id
                        userIds[email] = userId
                        AppLogger.info("Inserted test user: $email with ID: $userId")
                    } else {
                        // Get existing user ID
                        val existingUser = UsersTable.select { UsersTable.email eq email }.single()
                        userIds[email] = existingUser[UsersTable.id]
                        AppLogger.info("Found existing user: $email with ID: ${userIds[email]}")
                    }
                }

                // Insert test categories
                val testCategories = listOf(
                    Triple("Electronics", "Latest electronic devices and gadgets", true),
                    Triple("Clothing", "Fashion and apparel for all ages", true),
                    Triple("Books", "Books, magazines, and educational materials", true)
                )

                testCategories.forEach { (name, desc, visible) ->
                    if (CategoriesTable.select { CategoriesTable.name eq name }.empty()) {
                        CategoriesTable.insert {
                            it[CategoriesTable.name] = name
                            it[description] = desc
                            it[productCount] = 0L  // Initialize with 0 products
                            it[isVisible] = visible
                            it[imageUrl] = "/images/categories/${name.lowercase()}.png"
                        }
                        AppLogger.info("Inserted test category: $name")
                    }
                }

                // Insert test products
                val testProducts = listOf(
                    mapOf(
                        "name" to "Smartphone Pro",
                        "description" to "Latest smartphone with advanced features",
                        "price" to 899L,
                        "categoryName" to "Electronics",
                        "categoryId" to 1L,
                        "brand" to "TechBrand",
                        "stock" to 50L,
                        "discount" to 10L,
                        "rating" to 4.5,
                        "color" to "Black",
                        "featured" to true
                    ),
                    mapOf(
                        "name" to "Gaming Laptop",
                        "description" to "High-performance laptop for gaming",
                        "price" to 1299L,
                        "categoryName" to "Electronics",
                        "categoryId" to 1L,
                        "brand" to "GameTech",
                        "stock" to 25L,
                        "discount" to 15L,
                        "rating" to 4.7,
                        "color" to "Silver",
                        "featured" to true
                    ),
                    mapOf(
                        "name" to "Casual T-Shirt",
                        "description" to "Comfortable cotton t-shirt",
                        "price" to 29L,
                        "categoryName" to "Clothing",
                        "categoryId" to 2L,
                        "brand" to "StyleWear",
                        "stock" to 100L,
                        "discount" to 5L,
                        "rating" to 4.2,
                        "color" to "Blue",
                        "featured" to false
                    ),
                    mapOf(
                        "name" to "Programming Guide",
                        "description" to "Complete guide to modern programming",
                        "price" to 49L,
                        "categoryName" to "Books",
                        "categoryId" to 3L,
                        "brand" to "TechBooks",
                        "stock" to 75L,
                        "discount" to 20L,
                        "rating" to 4.8,
                        "color" to "Multi",
                        "featured" to true
                    )
                )

                testProducts.forEach { product ->
                    if (ProductsTable.select { ProductsTable.name eq product["name"].toString() }
                            .empty()) {
                        ProductsTable.insert {
                            it[name] = product["name"].toString()
                            it[description] = product["description"].toString()
                            it[price] = product["price"] as Long
                            it[imageUrl] = "/images/products/${
                                product["name"].toString().lowercase().replace(" ", "-")
                            }.png"
                            it[categoryName] = product["categoryName"].toString()
                            it[categoryId] = product["categoryId"] as Long
                            it[createdDate] = "2025-01-01"
                            it[updatedDate] = "2025-01-07"
                            it[totalStock] = product["stock"] as Long
                            it[brand] = product["brand"].toString()
                            it[isAvailable] = true
                            it[discount] = product["discount"] as Long
                            it[promotion] = "Test Promotion"
                            it[productRating] = product["rating"] as Double
                            it[color] = product["color"].toString()
                            it[isFeatured] = product["featured"] as Boolean
                            it[sold] = 0L  // Initialize with 0 sold
                        }
                        AppLogger.info("Inserted test product: ${product["name"]}")
                    }
                }

                // Insert test services
                val testServices = listOf(
                    mapOf(
                        "name" to "Web Development",
                        "description" to "Custom website development services",
                        "price" to 500L,
                        "offered" to 10L,
                        "category" to "Technology"
                    ),
                    mapOf(
                        "name" to "Graphic Design",
                        "description" to "Professional graphic design services",
                        "price" to 300L,
                        "offered" to 15L,
                        "category" to "Design"
                    ),
                    mapOf(
                        "name" to "Digital Marketing",
                        "description" to "Complete digital marketing solutions",
                        "price" to 750L,
                        "offered" to 8L,
                        "category" to "Marketing"
                    )
                )

                testServices.forEach { service ->
                    if (EservicesTable.select { EservicesTable.name eq service["name"].toString() }
                            .empty()) {
                        EservicesTable.insert {
                            it[name] = service["name"].toString()
                            it[description] = service["description"].toString()
                            it[price] = service["price"] as Long
                            it[offered] = service["offered"] as Long
                            it[category] = service["category"].toString()
                            it[imageUrl] = "/images/services/${
                                service["name"].toString().lowercase().replace(" ", "-")
                            }.png"
                            it[isVisible] = true
                            it[createdAt] = "2025-01-01"
                            it[updatedAt] = "2025-01-07"
                        }
                        AppLogger.info("Inserted test service: ${service["name"]}")
                    }
                }

                // Insert test promotions
                val testPromotions = listOf(
                    mapOf(
                        "title" to "New Year Super Sale",
                        "description" to "Get up to 50% off on selected items",
                        "startDate" to System.currentTimeMillis() - 86400000, // Yesterday
                        "endDate" to System.currentTimeMillis() + 2592000000, // 30 days from now
                        "enabled" to true
                    ),
                    mapOf(
                        "title" to "Spring Fashion Week",
                        "description" to "Latest fashion trends with amazing discounts",
                        "startDate" to System.currentTimeMillis(),
                        "endDate" to System.currentTimeMillis() + 1209600000, // 14 days from now
                        "enabled" to true
                    )
                )

                testPromotions.forEach { promo ->
                    if (PromotionTable.select { PromotionTable.title eq promo["title"].toString() }
                            .empty()) {
                        PromotionTable.insert {
                            it[title] = promo["title"].toString()
                            it[description] = promo["description"].toString()
                            it[imageUrl] = "/images/promotions/${
                                promo["title"].toString().lowercase().replace(" ", "-")
                            }.png"
                            it[startDate] = promo["startDate"] as Long
                            it[endDate] = promo["endDate"] as Long
                            it[enabled] = promo["enabled"] as Boolean
                        }
                        AppLogger.info("Inserted test promotion: ${promo["title"]}")
                    }
                }

                // Insert test prints
                val testPrints = listOf(
                    mapOf(
                        "name" to "Business Cards",
                        "description" to "Professional business card printing",
                        "price" to 25.99,
                        "copies" to 100
                    ),
                    mapOf(
                        "name" to "Flyers",
                        "description" to "High-quality flyer printing service",
                        "price" to 45.99,
                        "copies" to 500
                    ),
                    mapOf(
                        "name" to "Posters",
                        "description" to "Large format poster printing",
                        "price" to 75.99,
                        "copies" to 50
                    )
                )

                testPrints.forEach { print ->
                    if (PrintsTable.select { PrintsTable.name eq print["name"].toString() }
                            .empty()) {
                        PrintsTable.insert {
                            it[name] = print["name"].toString()
                            it[description] = print["description"].toString()
                            it[price] = print["price"] as Double
                            it[imageUrl] = "/images/prints/${
                                print["name"].toString().lowercase().replace(" ", "-")
                            }.png"
                            it[copies] = print["copies"] as Int
                        }
                        AppLogger.info("Inserted test print: ${print["name"]}")
                    }
                }

                // Insert test cart items
                if (CartTable.selectAll().empty()) {
                    CartTable.insert {
                        it[productId] = 1L
                        it[userId] = userIds["admin@example.com"]!!
                        it[quantity] = 2
                    }
                    CartTable.insert {
                        it[productId] = 2L
                        it[userId] = userIds["admin@example.com"]!!
                        it[quantity] = 1
                    }
                    CartTable.insert {
                        it[productId] = 3L
                        it[userId] = userIds["testuser1@example.com"]!!
                        it[quantity] = 3
                    }
                    AppLogger.info("Inserted test cart items")
                }

                // Insert test orders
                if (OrderTable.selectAll().empty()) {
                    OrderTable.insert {
                        it[userId] = userIds["admin@example.com"]!!.toInt()
                        it[productIds] = 1
                        it[totalQuantity] = "2"
                        it[totalSum] = 1798
                        it[status] = "Completed"
                        it[paymentType] = "Credit Card"
                        it[indicatorColor] = "green"
                        it[orderDate] = "2025-01-01"
                        it[trackingNumber] = "TR001"
                    }
                    OrderTable.insert {
                        it[userId] = userIds["testuser1@example.com"]!!.toInt()
                        it[productIds] = 2
                        it[totalQuantity] = "1"
                        it[totalSum] = 1299
                        it[status] = "Processing"
                        it[paymentType] = "PayPal"
                        it[indicatorColor] = "orange"
                        it[orderDate] = "2025-01-02"
                        it[trackingNumber] = "TR002"
                    }
                    AppLogger.info("Inserted test orders")
                }

                AppLogger.info("✅ All test data inserted successfully!")
            }
        } catch (e: Exception) {
            AppLogger.error("❌ Failed to insert test data", e)
            throw e
        }
    }

    /**
     * Configures and creates a HikariCP DataSource with production-ready settings.
     */
    private fun hikariDataSource(): HikariDataSource {
        val config = HikariConfig()

        // Production database configuration
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = DB_URL
        config.username = DB_USERNAME
        config.password = DB_PASSWORD

        // Production pool configuration
        config.maximumPoolSize = if (IS_PRODUCTION) 20 else 10
        config.minimumIdle = if (IS_PRODUCTION) 5 else 2
        config.idleTimeout = 300000 // 5 minutes
        config.connectionTimeout = 20000 // 20 seconds
        config.maxLifetime = 1800000 // 30 minutes
        config.leakDetectionThreshold = 60000 // 1 minute

        // Production settings
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.poolName = "EcomoraHikariPool"

        // Connection validation
        config.validationTimeout = 5000
        config.connectionTestQuery = "SELECT 1"

        // SSL configuration for production
        if (IS_PRODUCTION) {
            config.addDataSourceProperty("ssl", "true")
            config.addDataSourceProperty("sslmode", "require")
        }

        AppLogger.info("Configured HikariCP with pool size: ${config.maximumPoolSize}")
        return HikariDataSource(config)
    }

    /**
     * A utility function that wraps database operations inside a coroutine and Exposed transaction.
     *
     * This ensures:
     * - DB operations run on the IO dispatcher.
     * - All operations are executed inside a transaction.
     *
     * @param block A lambda that contains the DB operation.
     * @return The result of the DB operation.
     */
    suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        // Run block inside an exposed transaction
        transaction {
            block()
        }
    }

    /**
     * Clean shutdown of the database connection pool
     */
    fun close() {
        dataSource?.close()
        isInitialized = false
        AppLogger.info("Database connection pool closed")
    }
}