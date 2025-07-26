package est.ecomora.server.routes

import est.ecomora.server.domain.repository.order.OrderRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class OrderCreateRequest(
    val userId: Long,
    val indicatorColor: String,
    val productIds: Int,
    val totalQuantity: String,
    val totalSum: Int,
    val paymentType: String
)

@Serializable
data class OrderUpdateRequest(
    val orderProgress: String
)

@Serializable
data class OrderResponse(
    val message: String,
    val order: est.ecomora.server.domain.model.order.Order
)

fun Route.orderRoutes(
    db: OrderRepositoryImpl,
    servicesDb: EservicesRepositoryImpl
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val currentDateAndTime = Date()
    val calendar = Calendar.getInstance()
    calendar.time = currentDateAndTime
    calendar.add(Calendar.DAY_OF_YEAR, 4)
    val deliveryDate = calendar.time
    val formattedDeliveryDate: String = dateFormat.format(deliveryDate)

    route("v1/order") {
        post {
            try {
                val request = call.receive<OrderCreateRequest>()
                val trackingNumber = UUID.randomUUID().toString()

                val order = db.insertOrder(
                    userId = request.userId.toInt(),
                    productIds = request.productIds,
                    totalQuantity = request.totalQuantity,
                    totalSum = request.totalSum,
                    status = "On Progress",
                    paymentType = request.paymentType,
                    indicatorColor = request.indicatorColor,
                    orderDate = currentDateAndTime.toString(),
                    trackingNumber = trackingNumber
                )

                if (order != null) {
                    call.respond(
                        status = HttpStatusCode.Created,
                        OrderResponse("Order created successfully", order)
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Error while inserting order")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "BAD_REQUEST",
                        "Invalid JSON format or missing required fields: ${e.message}"
                    )
                )
            }
        }

        put("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Order ID Missing or Invalid")
                )

            try {
                val request = call.receive<OrderUpdateRequest>()

                // Check if order is being marked as completed
                if (request.orderProgress.equals("Completed", ignoreCase = true)) {
                    // Get the order to find out which service was ordered
                    val order = db.getOrderById(orderId)
                    if (order != null) {
                        // Get the service to find its owner
                        val service = servicesDb.getServiceById(
                            order.productIds.toLong(),
                            order.userId.toLong()
                        )
                        if (service != null) {
                            // Update the service sold counter
                            servicesDb.updateSoldCounter(
                                order.productIds.toLong(),
                                order.totalQuantity.toLong(),
                                service.userId
                            )
                        }
                    }
                }

                val updatedCount = db.updateOrderStatus(
                    id = orderId,
                    status = request.orderProgress,
                    currentTimestamp = System.currentTimeMillis().toString()
                )

                if (updatedCount > 0) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Order updated successfully")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Order not found or not updated")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse(
                        "BAD_REQUEST",
                        "Invalid JSON format or missing required fields: ${e.message}"
                    )
                )
            }
        }

        delete("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Order ID Missing or Invalid")
                )

            try {
                val deletedCount = db.deleteOrderById(orderId)
                if (deletedCount == 1) {
                    call.respond(HttpStatusCode.OK, SuccessResponse("Order deleted successfully"))
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Order not found or not deleted")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Deleting Order: ${e.message}")
                )
            }
        }

        get("userId/{userId}") {
            val userId = call.parameters["userId"]
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "userId Missing or Invalid")
                )

            try {
                val order = db.getAllOrdersByUserId(userId.toInt())
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "UserId not found for ID: $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Order: ${e.message}")
                )
            }
        }

        get("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Order ID Missing or Invalid")
                )

            try {
                val order = db.getOrderById(orderId)
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Order not found for ID: $orderId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Order: ${e.message}")
                )
            }
        }

        get {
            try {
                val order = db.getAllOrders()
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "No Orders Found Yet.")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error While Fetching Order: ${e.message}")
                )
            }
        }
    }
}