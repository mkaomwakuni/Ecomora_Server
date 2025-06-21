package est.tunzo.cyberpros.server.plugins

import est.tunzo.cyberpros.server.data.local.table.DatabaseFactory
import est.tunzo.cyberpros.server.domain.repository.users.UsersRepository
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    DatabaseFactory.init()
    val db = UsersRepository()
    routing {
        get("/") {
            call.respondText("Hello Enterpreneur!")
        }
        users(db)
    }
}