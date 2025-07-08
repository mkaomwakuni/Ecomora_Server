package est.ecomora.server.plugins

import est.ecomora.server.data.local.table.DatabaseFactory
import io.ktor.server.application.*

/**
 * Configures database connection and initializes tables.
 *
 * This function sets up the database connection and creates all necessary tables
 * for the Ecomora e-commerce application including users, products, categories,
 * orders, cart, promotions, services, and prints.
 *
 * @receiver Application The Ktor application context
 */
fun Application.configureDatabases() {
    DatabaseFactory.init()
}