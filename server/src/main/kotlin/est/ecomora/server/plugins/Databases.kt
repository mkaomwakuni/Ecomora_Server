package est.ecomora.server.plugins

import est.ecomora.server.data.local.table.DatabaseFactory
import io.ktor.server.application.*

// Database setup
fun Application.configureDatabases() {
    DatabaseFactory.init()
}