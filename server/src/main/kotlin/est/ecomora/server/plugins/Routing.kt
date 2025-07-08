package est.ecomora.server.plugins

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.domain.model.prints.PrintsRepositoryImpl
import est.ecomora.server.domain.repository.cart.CartRepositoryImpl
import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import est.ecomora.server.domain.repository.order.OrderRepositoryImpl
import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.domain.repository.promotions.PromotionsRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.domain.repository.users.UsersRepositoryImpl
import est.ecomora.server.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import est.ecomora.server.plugins.AppLogger 

fun Application.configureRouting() {
    // Initialize database
    DatabaseFactory.init()
    
    // Initialize repositories
    val usersRepository = UsersRepositoryImpl()
    val categoriesRepository = CategoriesRepositoryImpl()
    val productsRepository = ProductsRepositoryImpl()
    val eservicesRepository = EservicesRepositoryImpl()
    val printsRepository = PrintsRepositoryImpl()
    val promotionsRepository = PromotionsRepositoryImpl()
    val cartRepository = CartRepositoryImpl()
    val orderRepository = OrderRepositoryImpl()
    
    routing {
        // Health check endpoints
        healthRoutes()

        // API documentation endpoint
        get("/") {
            call.respondText("Hello Enterpreneur!")
        }

        // Register all route modules
        userRoutes(usersRepository)
        categoryRoutes(categoriesRepository)
        productRoutes(productsRepository)
        serviceRoutes(eservicesRepository)
        printRoutes(printsRepository)
        promotionRoutes(promotionsRepository)
        cartRoutes(cartRepository)
        orderRoutes(orderRepository)
        
        // Test routes for development
        testRoutes()
    }

    AppLogger.info("Routing configuration completed")
}