package est.ecomora.server.data.local.table

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.local.table.users.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Object responsible for setting up and managing the database connection using HikariCP and Exposed ORM.
 */
object DatabaseFactory {

    /**
     * Initializes the database connection.
     * - Connects to the database using the Hikari connection pool.
     * - Starts an initial (empty) transaction to ensure the connection is working.
     */

    fun init() {
        //Establish a connection to the database using the HikariCP connection pool.
        Database.connect(hikariDataSource())
        //Start an initial (empty) transaction to ensure the connection is working.
        transaction {
            Database.connect(hikariDataSource())
            transaction {
                SchemaUtils.create(UsersTable, CategoriesTable, ProductsTable)
            }
        }
    }



    /**
     * Configures and creates a HikariCP DataSource.
     *
     * @return A configured HikariDataSource used to connect to the database.
     */
    private fun  hikariDataSource(): HikariDataSource {
        val config = HikariConfig()


        // Set the JDBC driver class name from environment variables (e.g. org.postgresql.Driver)
        config.driverClassName = System.getenv("JDBC_DRIVER")

        // Set the JDBC database URL from environment variables (e.g. jdbc:postgresql://localhost:5432/db)
        config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")

        // Maximum number of connections in the pool
        config.maximumPoolSize = 3

        // Don't commit transactions automatically; they must be committed manually
        config.isAutoCommit = false

        // Set transaction isolation level to repeatable read (prevents some types of concurrency issues)
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

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
    suspend fun  <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        //Run block inside an exposed transaction
        transaction {
            block()
        }
    }
}