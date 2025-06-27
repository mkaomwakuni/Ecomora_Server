package est.ecomora.server.plugins


import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import est.ecomora.server.domain.repository.users.UsersRepository
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    DatabaseFactory.init()
    val db = UsersRepository()
    val categoriesDb = CategoriesRepositoryImpl()
    routing {
        get("/") {
            call.respondText("Hello Enterpreneur!")
        }
        users(db)
        category(categoriesDb)
    }
}