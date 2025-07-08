package est.ecomora.server.routes

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.cart.CartTable
import est.ecomora.server.data.local.table.category.CategoriesTable
import est.ecomora.server.data.local.table.order.OrderTable
import est.ecomora.server.data.local.table.prints.PrintsTable
import est.ecomora.server.data.local.table.products.ProductsTable
import est.ecomora.server.data.local.table.promotions.PromotionTable
import est.ecomora.server.data.local.table.services.EservicesTable
import est.ecomora.server.data.local.table.users.UsersTable
import est.ecomora.server.plugins.AppLogger
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.testRoutes() {
    route("test") {
        get("endpoints") {
            call.respondText(
                """
                # Ecomora Server API Endpoints Test Guide
                
                ## Authentication Endpoints
                POST /api/v1/auth/register - Register new user
                POST /api/v1/auth/login - User login
                
                ## User Endpoints
                GET /api/v1/users - Get all users
                GET /api/v1/users/{id} - Get user by ID
                DELETE /api/v1/users/{id} - Delete user
                
                ## Category Endpoints
                GET /v1/categories - Get all categories
                GET /v1/categories/{id} - Get category by ID
                POST /v1/categories - Create new category (multipart)
                PUT /v1/categories/{id} - Update category (multipart)
                DELETE /v1/categories/{id} - Delete category
                
                ## Product Endpoints
                GET /v1/products - Get all products
                GET /v1/products/{id} - Get product by ID
                GET /v1/products/userId/{ids} - Get products by IDs
                GET /v1/products/multiple?ids=1,2,3 - Get multiple products
                POST /v1/products - Create new product (multipart)
                PUT /v1/products/{id} - Update product (multipart)
                DELETE /v1/products/{id} - Delete product
                
                ## Service Endpoints
                GET /v1/services - Get all services
                GET /v1/services/{id} - Get service by ID
                POST /v1/services - Create new service (multipart)
                PUT /v1/services/{id} - Update service (multipart)
                DELETE /v1/services/{id} - Delete service
                
                ## Promotion Endpoints
                GET /v1/promotions - Get all promotions
                GET /v1/promotions/{id} - Get promotion by ID
                POST /v1/promotions - Create new promotion (multipart)
                PUT /v1/promotions/{id} - Update promotion (multipart)
                DELETE /v1/promotions/{id} - Delete promotion
                
                ## Print Endpoints
                GET /v1/prints - Get all prints
                GET /v1/prints/{id} - Get print by ID
                POST /v1/prints - Create new print (multipart)
                PUT /v1/prints/{id} - Update print (multipart)
                
                ## Cart Endpoints
                GET /v1/cart - Get all cart items
                GET /v1/cart/{userId} - Get cart items by user ID
                GET /v1/cart/user/{userId} - Get cart by user ID
                POST /v1/cart - Add item to cart
                PUT /v1/cart/{userId} - Update cart item
                PUT /v1/cart/item/{cartId} - Update cart item by cart ID
                DELETE /v1/cart/{userId} - Delete cart items by user ID
                DELETE /v1/cart/item/{cartId} - Delete cart item by cart ID
                
                ## Order Endpoints
                GET /v1/order - Get all orders
                GET /v1/order/{orderId} - Get order by ID
                GET /v1/order/userId/{userId} - Get orders by user ID
                POST /v1/order - Create new order
                PUT /v1/order/{orderId} - Update order status
                DELETE /v1/order/{orderId} - Delete order
                
                ## Test Data
                GET /test/data - View test data counts
                POST /test/reset-data - Reset and recreate test data
                """.trimIndent(),
                ContentType.Text.Plain
            )
        }

        get("data") {
            try {
                val dataCounts = transaction {
                    mapOf(
                        "users" to UsersTable.selectAll().count(),
                        "categories" to CategoriesTable.selectAll().count(),
                        "products" to ProductsTable.selectAll().count(),
                        "services" to EservicesTable.selectAll().count(),
                        "promotions" to PromotionTable.selectAll().count(),
                        "prints" to PrintsTable.selectAll().count(),
                        "cart_items" to CartTable.selectAll().count(),
                        "orders" to OrderTable.selectAll().count()
                    )
                }
                call.respond(HttpStatusCode.OK, dataCounts)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching data counts: ${e.message}")
            }
        }

        post("reset-data") {
            try {
                transaction {
                    // Delete all data in correct order (respecting foreign keys)
                    OrderTable.deleteWhere { OrderTable.id greater 0 }
                    CartTable.deleteWhere { CartTable.cartId greater 0 }
                    PrintsTable.deleteWhere { PrintsTable.id greater 0 }
                    PromotionTable.deleteWhere { PromotionTable.id greater 0 }
                    ProductsTable.deleteWhere { ProductsTable.id greater 0 }
                    EservicesTable.deleteWhere { EservicesTable.id greater 0 }
                    CategoriesTable.deleteWhere { CategoriesTable.id greater 0 }
                    UsersTable.deleteWhere { UsersTable.id greater 0 }

                    AppLogger.info("All test data cleared")
                }

                // Re-insert fresh test data
                DatabaseFactory.insertTestData()

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "message" to "Test data has been reset successfully",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                AppLogger.error("Error resetting test data", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "error" to "Failed to reset test data: ${e.message}",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
        }

        get("sample-requests") {
            call.respondText(
                """
                # Sample API Requests
                
                ## User Registration
                POST /api/v1/auth/register
                Content-Type: application/x-www-form-urlencoded
                
                email=newuser@example.com&password=password123&userName=newuser&fullName=New User&phoneNumber=+1234567893&userRole=user
                
                ## User Login
                POST /api/v1/auth/login
                Content-Type: application/x-www-form-urlencoded
                
                email=testuser1@example.com&password=password123
                
                ## Add to Cart
                POST /v1/cart
                Content-Type: application/x-www-form-urlencoded
                
                productId=1&userId=1&quantity=2
                
                ## Create Order
                POST /v1/order
                Content-Type: application/x-www-form-urlencoded
                
                userId=1&indicatorColor=blue&productIds=1&totalQuantity=2&totalSum=1798&paymentType=Credit Card
                
                ## Test Data Available:
                - Users: testuser1@example.com (password: password123)
                - Users: admin@example.com (password: admin123)
                - Users: testuser2@example.com (password: password456)
                - Categories: Electronics, Clothing, Books
                - Products: Smartphone Pro, Laptop Gaming, Casual T-Shirt, Programming Guide
                - Services: Web Development, Graphic Design, Digital Marketing
                - Promotions: New Year Super Sale, Spring Fashion Week
                - Prints: Business Cards, Flyers, Posters
                """.trimIndent(),
                ContentType.Text.Plain
            )
        }
    }
}