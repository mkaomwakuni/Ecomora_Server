package est.ecomora.server.plugins


import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.domain.repository.promotions.PromotionsRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.domain.repository.users.UsersRepositoryImpl
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    DatabaseFactory.init()
    val db = UsersRepositoryImpl()
    val categoriesDb = CategoriesRepositoryImpl()
    val productsDb = ProductsRepositoryImpl()
    val eservices = EservicesRepositoryImpl()
    val promotionsCampaingns = PromotionsRepositoryImpl()
    routing {
        get("/") {
            call.respondText("Hello Enterpreneur!")
        }
        users(db)
        category(categoriesDb)
        products(productsDb)
        promotions(promotionsCampaingns)
        services(eservices)
    }
}