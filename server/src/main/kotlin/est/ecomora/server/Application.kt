package est.ecomora.server

import est.ecomora.server.plugins.configureDatabases
import est.ecomora.server.plugins.configureHTTP
import est.ecomora.server.plugins.configureMonitoring
import est.ecomora.server.plugins.configureRouting
import est.ecomora.server.plugins.configureSecurity
import est.ecomora.server.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
    configureMonitoring()
    configureDatabases()
    configureSecurity()
}
