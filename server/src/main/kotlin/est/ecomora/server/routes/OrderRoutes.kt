package est.ecomora.server.routes

import est.ecomora.server.domain.repository.order.OrderRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*

fun Route.orderRoutes(
    db: OrderRepositoryImpl
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
            val parameters = call.receive<Parameters>()
            val userId = parameters["userId"]
                ?: return@post call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val indicatorColor = parameters["indicatorColor"]
                ?: return@post call.respondText(
                    text = "Indicator Color Missing",
                    status = HttpStatusCode.BadRequest
                )
            val productIds = parameters["productIds"]?.toInt()
                ?: return@post call.respondText(
                    text = "Product IDs Missing",
                    status = HttpStatusCode.BadRequest
                )
            val totalQuantity = parameters["totalQuantity"]
                ?: return@post call.respondText(
                    text = "Total Quantity Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val totalSum = parameters["totalSum"]?.toInt()
                ?: return@post call.respondText(
                    text = "Total Price Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val paymentType = parameters["paymentType"]
                ?: return@post call.respondText(
                    text = "Payment Type Missing",
                    status = HttpStatusCode.BadRequest
                )
            val trackingNumber = UUID.randomUUID().toString()
            try {
                val order = db.insertOrder(
                    userId = userId.toInt(),
                    productIds = productIds,
                    totalQuantity = totalQuantity,
                    totalSum = totalSum,
                    status = "On Progress",
                    paymentType = paymentType,
                    indicatorColor = indicatorColor,
                    orderDate = currentDateAndTime.toString(),
                    trackingNumber = trackingNumber
                )
                order?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respondText(
                    text = "Error while inserting order",
                    status = HttpStatusCode.InternalServerError
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Inserting Order: ${e.message}"
                )
            }
        }

        put("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@put call.respondText(
                    text = "Order ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            val parameters = call.receive<Parameters>()

            val orderProgress = parameters["orderProgress"]
                ?: return@put call.respondText(
                    text = "Payment Type Missing",
                    status = HttpStatusCode.BadRequest
                )

            try {
                val updatedCount = db.updateOrderStatus(
                    id = orderId,
                    status = orderProgress,
                    currentTimestamp = System.currentTimeMillis().toString()
                )
                if (updatedCount > 0) {
                    call.respond(HttpStatusCode.OK, "Order updated successfully")
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Order not found or not updated"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Updating Order: ${e.message}"
                )
            }
        }

        delete("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@delete call.respondText(
                    text = "Order ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            try {
                val deletedCount = db.deleteOrderById(orderId)
                if (deletedCount ==1) {
                    call.respond(HttpStatusCode.OK, "Order deleted successfully")
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Order not found or not deleted"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Deleting Order: ${e.message}"
                )
            }
        }

        get("{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()
                ?: return@get call.respondText(
                    text = "Order ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            try {
                val order = db.getOrderById(orderId)
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Order not found for ID: $orderId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Order: ${e.message}"
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
                        "No Orders Found Yet."
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Order: ${e.message}"
                )
            }
        }

        get("userId/{userId}") {
            val orderId = call.parameters["userId"]
                ?: return@get call.respondText(
                    text = "userId  Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )

            try {
                val order = db.getAllOrdersByUserId(orderId.toInt())
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "UserId not found for ID: $orderId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Order: ${e.message}"
                )
            }
        }
    }
}