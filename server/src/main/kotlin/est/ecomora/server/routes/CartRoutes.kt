package est.ecomora.server.routes

import est.ecomora.server.domain.repository.cart.CartRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cartRoutes(
    db: CartRepositoryImpl,
) {
    route("v1/cart") {
        post {
            try {
                // Try to receive as Parameters first (form data)
                val parameters = try {
                    call.receive<Parameters>()
                } catch (e: Exception) {
                    // If that fails, try to receive as text and parse as form data
                    try {
                        val formData = call.receiveText()
                        val params = mutableMapOf<String, String>()
                        formData.split("&").forEach { pair ->
                            val (key, value) = pair.split("=", limit = 2)
                            params[key] = value
                        }
                        Parameters.build {
                            params.forEach { (key, value) -> append(key, value) }
                        }
                    } catch (e2: Exception) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid request format. Expected form data with productId, userId, and quantity."
                        )
                    }
                }

                val productId = parameters["productId"]?.toLongOrNull()
                    ?: return@post call.respondText(
                        text = "Product ID Missing or Invalid",
                        status = HttpStatusCode.BadRequest
                    )
                val quantity = parameters["quantity"]?.toIntOrNull()
                    ?: return@post call.respondText(
                        text = "Quantity Missing or Invalid",
                        status = HttpStatusCode.BadRequest
                    )
                val userId = parameters["userId"]?.toLongOrNull()
                    ?: return@post call.respondText(
                        text = "User ID Missing or Invalid",
                        status = HttpStatusCode.BadRequest
                    )

                val cartItem = db.insertCartItem(productId, userId, quantity)
                if (cartItem != null) {
                    call.respond(
                        status = HttpStatusCode.Created,
                        "Cart item added successfully: $cartItem"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        "Failed to add item to cart"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Adding Item to Cart: ${e.message}"
                )
            }
        }

        get {
            try {
                val cartItems = db.getAllCart()
                if (cartItems != null) {
                    call.respond(HttpStatusCode.OK, cartItems)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Cart Items Not Found for User ID: $cartItems"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Cart Items: ${e.message}"
                )
            }
        }

        get("user/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            try {
                val cartItems = db.getCartByUserId(userId)
                if (cartItems != null) {
                    call.respond(HttpStatusCode.OK, cartItems)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Cart Items Not Found for User ID: $userId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Cart Items: ${e.message}"
                )
            }
        }

        get("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            try {
                val cartItems = db.getCartItemByUserId(userId)
                if (cartItems != null) {
                    call.respond(HttpStatusCode.OK, cartItems)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Cart Items Not Found for User ID: $userId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Cart Items: ${e.message}"
                )
            }
        }

        delete("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@delete call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            try {
                val deletedItemsCount = db.deleteCartItemByUserId(userId)
                call.respond(
                    status = HttpStatusCode.OK,
                    "Deleted $deletedItemsCount items from cart for user ID: $userId"
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Deleting Cart Items: ${e.message}"
                )
            }
        }

        put("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@put call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            val parameters = call.receive<Parameters>()
            val cartId = parameters["cartId"]?.toIntOrNull()
                ?: return@put call.respondText(
                    text = "cart ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val productId = parameters["productId"]?.toLongOrNull()
                ?: return@put call.respondText(
                    text = "Product ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val quantity = parameters["quantity"]?.toIntOrNull()
                ?: return@put call.respondText(
                    text = "Quantity Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            try {
                db.updateCart(cartId, productId, userId, quantity)
                call.respond(HttpStatusCode.OK, "Cart item updated successfully")
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Updating Cart Item: ${e.message}"
                )
            }
        }

        delete("item/{cartId}") {
            val cartId = call.parameters["cartId"]?.toIntOrNull()
                ?: return@delete call.respondText(
                    text = "Cart ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            try {
                val deletedItemsCount = db.deleteCartItemByCartId(cartId)
                if (deletedItemsCount == 1) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        "Deleted $deletedItemsCount items from cart with ID: $cartId"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Cart Item Not Found for Cart ID: $cartId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Deleting Cart Item: ${e.message}"
                )
            }
        }

        put("item/{cartId}") {
            val cartId = call.parameters["cartId"]?.toIntOrNull()
                ?: return@put call.respondText(
                    text = "Cart ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            val parameters = call.receive<Parameters>()
            val productId = parameters["productId"]?.toLongOrNull()
                ?: return@put call.respondText(
                    text = "Product ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val quantity = parameters["quantity"]?.toIntOrNull()
                ?: return@put call.respondText(
                    text = "Quantity Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val userId = parameters["userId"]?.toIntOrNull()
                ?: return@put call.respondText(
                    text = "userId Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            try {
                db.updateCartItem(cartId, productId, userId.toLong(),quantity)
                call.respond(HttpStatusCode.OK, "Cart item updated successfully")
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Updating Cart Item: ${e.message}"
                )
            }
        }
    }
}