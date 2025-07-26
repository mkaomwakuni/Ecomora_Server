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


    /**
     * Configures and creates a HikariCP DataSource with production-ready settings.
     */
    private fun hikariDataSource(): HikariDataSource {
        val config = HikariConfig()

        if (IS_PRODUCTION) {
            // Production: PostgreSQL configuration
            config.driverClassName = "org.postgresql.Driver"
            config.jdbcUrl = DB_URL
            config.username = DB_USERNAME
            config.password = DB_PASSWORD
        } else {
            // Development: use an embedded/file-based H2 database
            // Path can be customised via H2_PATH env var, otherwise defaults to user-home directory
            val h2Path = System.getenv("H2_PATH")
                ?: "${System.getProperty("user.home")}/ecomora/h2/ecomorah_db"
            config.driverClassName = "org.h2.Driver"
            // Use PostgreSQL compatibility mode so that Exposed DDL remains similar
            config.jdbcUrl =
                "jdbc:h2:file:$h2Path;AUTO_SERVER=TRUE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
            config.username = "sa"
            config.password = ""
        }

        // Common pool configuration
        config.maximumPoolSize = if (IS_PRODUCTION) 20 else 10
        config.minimumIdle = if (IS_PRODUCTION) 5 else 2
        config.idleTimeout = 300000 // 5 minutes
        config.connectionTimeout = 20000 // 20 seconds
        config.maxLifetime = 1800000 // 30 minutes
        config.leakDetectionThreshold = 60000 // 1 minute

        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.poolName = "EcomoraHikariPool"

        // Connection validation
        config.validationTimeout = 5000
        config.connectionTestQuery = "SELECT 1"

        // SSL only for production/PostgreSQL
        if (IS_PRODUCTION) {
            config.addDataSourceProperty("ssl", "true")
            config.addDataSourceProperty("sslmode", "require")
        }

        AppLogger.info("Configured HikariCP (${if (IS_PRODUCTION) "PostgreSQL" else "H2"}) with pool size: ${config.maximumPoolSize}")
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