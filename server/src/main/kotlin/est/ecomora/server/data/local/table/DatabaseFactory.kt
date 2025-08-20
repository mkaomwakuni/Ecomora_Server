package est.ecomora.server.data.local.table

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import est.ecomora.server.DB_PASSWORD
import est.ecomora.server.DB_URL
import est.ecomora.server.DB_USERNAME
import est.ecomora.server.IS_PRODUCTION
import est.ecomora.server.data.local.table.cart.CartTable
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.local.table.order.OrderTable
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.local.table.promotions.PromotionTable
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.local.table.users.UsersTable
import est.ecomora.server.plugins.AppLogger
import est.ecomora.server.plugins.DatabaseLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private var dataSource: HikariDataSource? = null
    private var isInitialized = false

    // DB initialization entry point
    fun init() {
        if (isInitialized) {
            AppLogger.info("Database already initialized, skipping...")
            return
        }

        try {
            // Setup HikariCP
            dataSource = hikariDataSource()

            Database.connect(dataSource!!)
            DatabaseLogger.logConnection("Connected to database successfully")

            // Schema creation
            transaction {
                SchemaUtils.create(
                    UsersTable,
                    CategoriesTable,
                    PromotionTable,
                    EservicesTable,
                    ProductsTable,
                    CartTable,
                    OrderTable
                )

                AppLogger.info("Database tables created successfully")
            }

            isInitialized = true
            AppLogger.info("Database initialization completed successfully")

        } catch (e: Exception) {
            AppLogger.error("Database initialization failed", e)
            throw e
        }
    }


    // HikariCP configuration
    private fun hikariDataSource(): HikariDataSource {
        val config = HikariConfig()

        // Environment check
        AppLogger.info("ENV = '${System.getenv("ENV")}', IS_PRODUCTION = $IS_PRODUCTION, DB_URL = '$DB_URL'")

        if (IS_PRODUCTION) {
            // PostgreSQL
            config.driverClassName = "org.postgresql.Driver"
            config.jdbcUrl = DB_URL
            config.username = DB_USERNAME
            config.password = DB_PASSWORD
        } else {
            // H2 development DB
            val h2Path = System.getenv("H2_PATH")
                ?: "${System.getProperty("user.home")}/ecomora/h2/ecomorah_db"
            config.driverClassName = "org.h2.Driver"
            config.jdbcUrl =
                "jdbc:h2:file:$h2Path;AUTO_SERVER=TRUE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
            config.username = "sa"
            config.password = ""
        }

        // Pool settings
        config.maximumPoolSize = if (IS_PRODUCTION) 20 else 10
        config.minimumIdle = if (IS_PRODUCTION) 5 else 2
        config.idleTimeout = 300000
        config.connectionTimeout = 20000
        config.maxLifetime = 1800000
        config.leakDetectionThreshold = 60000

        config.isAutoCommit = false
        // H2 only - PostgreSQL handles isolation differently
        if (!IS_PRODUCTION) {
            config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.poolName = "EcomoraHikariPool"

        // Validation
        config.validationTimeout = 5000
        config.connectionTestQuery = "SELECT 1"

        // PostgreSQL SSL
        if (IS_PRODUCTION) {
            config.addDataSourceProperty("ssl", "true")
            config.addDataSourceProperty("sslmode", "require")
        }

        AppLogger.info("Configured HikariCP (${if (IS_PRODUCTION) "PostgreSQL" else "H2"}) with pool size: ${config.maximumPoolSize}")
        return HikariDataSource(config)
    }

    // Coroutine DB wrapper
    suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }

    // Cleanup
    fun close() {
        dataSource?.close()
        isInitialized = false
        AppLogger.info("Database connection pool closed")
    }
}