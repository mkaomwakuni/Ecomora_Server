package est.ecomora.server.routes

import est.ecomora.server.domain.repository.cart.CartRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val productId: Long,
    val userId: Long,
    val quantity: Int
)

@Serializable
data class UpdateCartRequest(
    val productId: Long,
    val quantity: Int
)

@Serializable
data class UpdateCartItemRequest(
    val productId: Long,
    val quantity: Int,
    val userId: Long
)

fun Route.cartRoutes(
    db: CartRepositoryImpl,
) {
    route("v1/cart") {
        post {
            try {
                val request = call.receive<AddToCartRequest>()

                val cartItem =
                    db.insertCartItem(request.productId, request.userId, request.quantity)
                if (cartItem != null) {
                    call.respond(
                        status = HttpStatusCode.Created,
                        SuccessResponse("Cart item added successfully: $cartItem")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Failed to add item to cart")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Invalid request format: ${e.message}")
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
                        ErrorResponse("NOT_FOUND", "Cart Items Not Found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Cart Items: ${e.message}")
                )
            }
        }

        get("user/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "User ID Missing or Invalid")
                )
            try {
                val cartItems = db.getCartByUserId(userId)
                if (cartItems != null) {
                    call.respond(HttpStatusCode.OK, cartItems)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Cart Items Not Found for User ID: $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Cart Items: ${e.message}")
                )
            }
        }

        get("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "User ID Missing or Invalid")
                )
            try {
                val cartItems = db.getCartItemByUserId(userId)
                if (cartItems != null) {
                    call.respond(HttpStatusCode.OK, cartItems)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Cart Items Not Found for User ID: $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Cart Items: ${e.message}")
                )
            }
        }

        delete("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "User ID Missing or Invalid")
                )
            try {
                val deletedItemsCount = db.deleteCartItemByUserId(userId)
                call.respond(
                    status = HttpStatusCode.OK,
                    SuccessResponse("Deleted $deletedItemsCount items from cart for user ID: $userId")
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Deleting Cart Items: ${e.message}")
                )
            }
        }

        put("{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "User ID Missing or Invalid")
                )

            try {
                val request = call.receive<UpdateCartRequest>()

                db.updateCart(userId.toInt(), request.productId, userId, request.quantity)
                call.respond(HttpStatusCode.OK, SuccessResponse("Cart item updated successfully"))
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "BAD_REQUEST",
                        "Invalid request format or missing fields: ${e.message}"
                    )
                )
            }
        }

        delete("item/{cartId}") {
            val cartId = call.parameters["cartId"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Cart ID Missing or Invalid")
                )
            try {
                val deletedItemsCount = db.deleteCartItemByCartId(cartId)
                if (deletedItemsCount == 1) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        SuccessResponse("Deleted $deletedItemsCount items from cart with ID: $cartId")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Cart Item Not Found for Cart ID: $cartId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Deleting Cart Item: ${e.message}")
                )
            }
        }

        put("item/{cartId}") {
            val cartId = call.parameters["cartId"]?.toIntOrNull()
                ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Cart ID Missing or Invalid")
                )

            try {
                val request = call.receive<UpdateCartItemRequest>()

                db.updateCartItem(cartId, request.productId, request.userId, request.quantity)
                call.respond(HttpStatusCode.OK, SuccessResponse("Cart item updated successfully"))
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "BAD_REQUEST",
                        "Invalid request format or missing fields: ${e.message}"
                    )
                )
            }
        }
    }
}