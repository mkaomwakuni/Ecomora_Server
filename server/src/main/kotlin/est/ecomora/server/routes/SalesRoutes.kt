package est.ecomora.server.routes

import est.ecomora.server.domain.repository.sales.SalesRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*

fun Route.salesRoutes(
    salesDb: SalesRepositoryImpl
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    
    route("v1/sales") {
        // Create a direct sale (instant transaction)
        post {
            val parameters = call.receive<Parameters>()
            
            val userId = parameters["userId"]?.toIntOrNull()
                ?: return@post call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            val itemId = parameters["itemId"]?.toLongOrNull()
                ?: return@post call.respondText(
                    text = "Item ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            val itemType = parameters["itemType"]
                ?: return@post call.respondText(
                    text = "Item Type Missing (product/service/print)",
                    status = HttpStatusCode.BadRequest
                )
            
            if (itemType !in listOf("product", "service", "print")) {
                return@post call.respondText(
                    text = "Invalid item type. Must be: product, service, or print",
                    status = HttpStatusCode.BadRequest
                )
            }
            
            val itemName = parameters["itemName"]
                ?: return@post call.respondText(
                    text = "Item Name Missing",
                    status = HttpStatusCode.BadRequest
                )
            
            val quantity = parameters["quantity"]?.toIntOrNull()
                ?: return@post call.respondText(
                    text = "Quantity Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            val unitPrice = parameters["unitPrice"]?.toLongOrNull()
                ?: return@post call.respondText(
                    text = "Unit Price Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            val paymentType = parameters["paymentType"]
                ?: return@post call.respondText(
                    text = "Payment Type Missing",
                    status = HttpStatusCode.BadRequest
                )
            
            val totalAmount = quantity * unitPrice
            val currentTime = System.currentTimeMillis()
            val saleDate = dateFormat.format(Date(currentTime))
            
            try {
                val sale = salesDb.insertSale(
                    userId = userId,
                    itemId = itemId,
                    itemType = itemType,
                    itemName = itemName,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    totalAmount = totalAmount,
                    paymentType = paymentType,
                    saleDate = saleDate,
                    timestamp = currentTime
                )
                
                sale?.let {
                    call.respond(HttpStatusCode.Created, it)
                } ?: call.respondText(
                    text = "Error while processing sale",
                    status = HttpStatusCode.InternalServerError
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Processing Sale: ${e.message}"
                )
            }
        }
        
        // Get all sales
        get {
            try {
                val sales = salesDb.getAllSales()
                call.respond(HttpStatusCode.OK, sales)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Sales: ${e.message}"
                )
            }
        }
        
        // Get sale by ID
        get("{saleId}") {
            val saleId = call.parameters["saleId"]?.toLongOrNull()
                ?: return@get call.respondText(
                    text = "Sale ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            try {
                val sale = salesDb.getSaleById(saleId)
                if (sale != null) {
                    call.respond(HttpStatusCode.OK, sale)
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Sale not found for ID: $saleId"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Sale: ${e.message}"
                )
            }
        }
        
        // Get sales by user ID
        get("user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: return@get call.respondText(
                    text = "User ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                )
            
            try {
                val sales = salesDb.getSalesByUserId(userId)
                call.respond(HttpStatusCode.OK, sales)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching User Sales: ${e.message}"
                )
            }
        }
        
        // Get sales by item type
        get("type/{itemType}") {
            val itemType = call.parameters["itemType"]
                ?: return@get call.respondText(
                    text = "Item Type Missing",
                    status = HttpStatusCode.BadRequest
                )
            
            try {
                val sales = salesDb.getSalesByItemType(itemType)
                call.respond(HttpStatusCode.OK, sales)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Fetching Sales by Type: ${e.message}"
                )
            }
        }
    }
}