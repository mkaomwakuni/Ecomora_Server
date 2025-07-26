package est.ecomora.server.routes

import est.ecomora.server.domain.model.services.EServices
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.plugins.getCurrentUserId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ServiceCreateRequest(
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String? = null,
    val categoryName: String,
    val categoryId: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val offered: Long = 0,
    val isVisible: Boolean = false,
    val discount: Long = 0,
    val promotion: String = ""
)

@Serializable
data class ServiceUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val imageUrl: String? = null,
    val categoryName: String? = null,
    val categoryId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val offered: Long? = null,
    val isVisible: Boolean? = null,
    val discount: Long? = null,
    val promotion: String? = null
)

@Serializable
data class ServiceResponse(
    val message: String,
    val service: EServices
)

fun Route.serviceRoutes(
    db: EservicesRepositoryImpl
) {
    route("v1/services") {
        authenticate("auth-jwt") {
            post {
                val userId = call.getCurrentUserId() ?: return@post call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val contentType = call.request.contentType()

                if (contentType.match(ContentType.MultiPart.FormData)) {
                    // Handle multipart/form-data (with file upload)
                    handleServiceCreateMultipart(call, db, userId)
                } else {
                    // Handle JSON
                    try {
                        val request = call.receive<ServiceCreateRequest>()

                        val service = db.insertService(
                            name = request.name,
                            description = request.description,
                            price = request.price,
                            offered = request.offered,
                            categoryName = request.categoryName,
                            categoryId = request.categoryId,
                            imageUrl = request.imageUrl ?: "/upload/services/default.jpg",
                            isVisible = request.isVisible,
                            createdAt = System.currentTimeMillis().toString(),
                            updatedAt = System.currentTimeMillis().toString(),
                            userId = userId,
                            discount = request.discount,
                            promotion = request.promotion
                        )

                        if (service?.id != null) {
                            call.respond(
                                status = HttpStatusCode.Created,
                                ServiceResponse("Service created successfully", service)
                            )
                        } else {
                            call.respond(
                                status = HttpStatusCode.InternalServerError,
                                ErrorResponse("INTERNAL_ERROR", "Failed to create service")
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
            }

            get {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                try {
                    val items = db.getAllServicesByUserId(userId)
                    if (items?.isNotEmpty() == true) {
                        call.respond(HttpStatusCode.OK, items)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No Items We Found")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Error Fetching Items ${e.message}")
                    )
                }
            }

            get("{id}") {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val id = call.parameters["id"] ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "No ID Found...")
                )
                try {
                    val service = db.getServiceById(id.toLong(), userId)
                    if (service == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No Services Found")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            service
                        )
                    }

                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            "INTERNAL_ERROR",
                            "Error While Fetching Services ${e.message}"
                        )
                    )
                }
            }

            delete("{id}") {
                val userId = call.getCurrentUserId() ?: return@delete call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Invalid Item ID")
                )
                try {
                    val items = db.deleteServiceById(id.toLong(), userId)
                    if (items == 1) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse("Item Deleted - Success")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Item Not Found - Failure")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Error Deleting Items ${e.message}")
                    )
                }
            }

            put("{id}") {
                val userId = call.getCurrentUserId() ?: return@put call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Invalid or Missing Service ID")
                )

                val contentType = call.request.contentType()

                if (contentType.match(ContentType.MultiPart.FormData)) {
                    // Handle multipart/form-data (with file upload)
                    handleServiceUpdateMultipart(call, db, userId, id)
                } else {
                    // Handle JSON
                    try {
                        val request = call.receive<ServiceUpdateRequest>()
                        val existingService = db.getServiceById(id, userId)
                            ?: return@put call.respond(
                                status = HttpStatusCode.NotFound,
                                ErrorResponse("NOT_FOUND", "Service with ID $id not found")
                            )

                        val result = db.updateService(
                            id = id,
                            name = request.name ?: existingService.name,
                            description = request.description ?: existingService.description,
                            price = request.price ?: existingService.price,
                            offered = request.offered ?: existingService.offered,
                            categoryName = request.categoryName ?: existingService.categoryName,
                            categoryId = request.categoryId ?: existingService.categoryId,
                            imageUrl = request.imageUrl ?: existingService.imageUrl,
                            isVisible = request.isVisible ?: existingService.isVisible,
                            createdAt = existingService.createdAt,
                            updatedAt = System.currentTimeMillis().toString(),
                            userId = userId,
                            discount = request.discount ?: existingService.discount,
                            promotion = request.promotion ?: existingService.promotion
                        )

                        if (result > 0) {
                            val updatedService = db.getServiceById(id, userId)
                            call.respond(
                                status = HttpStatusCode.OK,
                                ServiceResponse("Service updated successfully", updatedService!!)
                            )
                        } else {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                ErrorResponse("NOT_FOUND", "Service with ID $id not found")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            ErrorResponse("BAD_REQUEST", "Invalid JSON format: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

private suspend fun handleServiceCreateMultipart(
    call: ApplicationCall, 
    db: EservicesRepositoryImpl, 
    userId: Long
) {
    val multipart = call.receiveMultipart()
    var name: String? = null
    var description: String? = null
    var price: Long? = null
    var imageUrl: String? = null
    var categoryName: String? = null
    var categoryId: Long? = null
    var offered: Long? = 0
    var isVisible: Boolean? = null
    var discount: Long? = 0
    var promotion: String? = ""
    val uploadsDirectory = File("upload/services/")
    if (!uploadsDirectory.exists()) {
        uploadsDirectory.mkdirs()
    }

    multipart.forEachPart { segmentsData ->
        when (segmentsData) {
            is PartData.FileItem -> {
                val fileName = segmentsData.originalFileName?.replace(" ", "_")
                    ?: "Image${System.currentTimeMillis()}"
                val file = File(uploadsDirectory, fileName)
                segmentsData.streamProvider().use { input ->
                    file.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }
                imageUrl = "/upload/services/$fileName"
            }

            is PartData.FormItem -> {
                when (segmentsData.name) {
                    "name" -> name = segmentsData.value
                    "description" -> description = segmentsData.value
                    "price" -> price = segmentsData.value.toLongOrNull()
                    "imageUrl" -> imageUrl = segmentsData.value
                    "categoryName" -> categoryName = segmentsData.value
                    "categoryId" -> categoryId = segmentsData.value.toLongOrNull()
                    "offered" -> offered = segmentsData.value.toLongOrNull()
                    "isVisible" -> isVisible =
                        segmentsData.value.toBooleanStrictOrNull()
                    "discount" -> discount = segmentsData.value.toLongOrNull()
                    "promotion" -> promotion = segmentsData.value
                }
            }

            else -> {
            }
        }
    }
    try {
        val service = db.insertService(
            name ?: throw Exception("Name Missing"),
            description ?: throw Exception("Description Missing"),
            price ?: throw Exception("Price Missing or Invalid"),
            offered ?: 0,
            categoryName ?: throw Exception("Category Name Missing"),
            categoryId ?: throw Exception("Category ID Missing or Invalid"),
            imageUrl ?: "/upload/services/default.jpg", // default image
            isVisible ?: false,
            System.currentTimeMillis().toString(),
            System.currentTimeMillis().toString(),
            userId,
            discount ?: 0,
            promotion ?: ""
        )
        service?.id?.let {
            call.respond(
                status = HttpStatusCode.Created,
                service
            )
        } ?: call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse("INTERNAL_ERROR", "Failed to create service")
        )
    } catch (e: Exception) {
        call.respond(
            status = HttpStatusCode.BadRequest,
            ErrorResponse("BAD_REQUEST", e.message ?: "An error occurred")
        )
    }
}

private suspend fun handleServiceUpdateMultipart(
    call: ApplicationCall, 
    db: EservicesRepositoryImpl, 
    userId: Long,
    id: Long
) {
    val multipart = call.receiveMultipart()
    var name: String? = null
    var description: String? = null
    var price: Long? = null
    var imageUrl: String? = null
    var categoryName: String? = null
    var categoryId: Long? = null
    var offered: Long? = 0
    var isVisible: Boolean? = null
    var discount: Long? = null
    var promotion: String? = null

    multipart.forEachPart { partData ->
        when (partData) {
            is PartData.FileItem -> {
                val fileName = partData.originalFileName?.replace(" ", "_")
                    ?: "Image${System.currentTimeMillis()}"
                val file = File("upload/services", fileName)
                partData.streamProvider().use { input ->
                    file.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }
                imageUrl = "/upload/services/$fileName"
            }
            is PartData.FormItem -> {
                when (partData.name) {
                    "name" -> name = partData.value
                    "description" -> description = partData.value
                    "price" -> price = partData.value.toLongOrNull()
                    "imageUrl" -> imageUrl = partData.value
                    "categoryName" -> categoryName = partData.value
                    "categoryId" -> categoryId = partData.value.toLongOrNull()
                    "offered" -> offered = partData.value.toLongOrNull()
                    "isVisible" -> isVisible = partData.value.toBooleanStrictOrNull()
                    "discount" -> discount = partData.value.toLongOrNull()
                    "promotion" -> promotion = partData.value
                }
            }
            else -> {
            }
        }
        partData.dispose()
    }

    try {
        // Get existing service to use as defaults for missing fields
        val existingService = db.getServiceById(id, userId)
            ?: return call.respond(
                status = HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", "Service with ID $id not found")
            )

        val result = db.updateService(
            id,
            name ?: existingService.name,
            description ?: existingService.description,
            price ?: existingService.price,
            offered ?: existingService.offered,
            categoryName ?: existingService.categoryName,
            categoryId ?: existingService.categoryId,
            imageUrl ?: existingService.imageUrl,
            isVisible ?: existingService.isVisible,
            existingService.createdAt,
            System.currentTimeMillis().toString(),
            userId,
            discount ?: existingService.discount,
            promotion ?: existingService.promotion
        )

        if (result > 0) {
            val updatedService = db.getServiceById(id, userId)
            call.respond(
                status = HttpStatusCode.OK,
                ServiceResponse("Service Updated Successfully", updatedService!!)
            )
        } else {
            call.respond(
                status = HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", "Service with ID $id not found")
            )
        }
    } catch (e: Exception) {
        call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse(
                "INTERNAL_ERROR",
                "Error While Updating Service: ${e.message}"
            )
        )
    }
}