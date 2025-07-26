package est.ecomora.server.routes

import est.ecomora.server.domain.repository.promotions.PromotionsRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.text.SimpleDateFormat

@Serializable
data class PromotionCreateRequest(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val startDate: String,
    val endDate: String,
    val enabled: Boolean
)

@Serializable
data class PromotionUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val enabled: Boolean? = null
)

@Serializable
data class PromotionResponse(
    val message: String,
    val promotion: est.ecomora.server.domain.model.promotions.Promotions
)

fun Route.promotionRoutes(
    db: PromotionsRepositoryImpl
) {
    route("v1/promotions") {
        // Create promotion - supports both JSON and multipart
        post {
            val contentType = call.request.contentType()

            if (contentType.match(ContentType.MultiPart.FormData)) {
                // Handle multipart/form-data (with file upload)
                handlePromotionCreateMultipart(call, db)
            } else {
                // Handle JSON
                try {
                    val request = call.receive<PromotionCreateRequest>()
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy")

                    val startDate = try {
                        // Try to parse as timestamp first, then as formatted date
                        request.startDate.toLongOrNull()
                            ?: dateFormat.parse(request.startDate)?.time
                    } catch (e: Exception) {
                        null
                    } ?: return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "BAD_REQUEST",
                            "Invalid start date format. Use timestamp or MM/dd/yyyy"
                        )
                    )

                    val endDate = try {
                        // Try to parse as timestamp first, then as formatted date
                        request.endDate.toLongOrNull() ?: dateFormat.parse(request.endDate)?.time
                    } catch (e: Exception) {
                        null
                    } ?: return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "BAD_REQUEST",
                            "Invalid end date format. Use timestamp or MM/dd/yyyy"
                        )
                    )

                    val promotion = db.insertPromo(
                        userId = 1L, // Default admin user for global promotions
                        title = request.title,
                        description = request.description,
                        imageUrl = request.imageUrl ?: "/upload/products/promotions/default.jpg",
                        startDate = startDate,
                        endDate = endDate,
                        enabled = request.enabled
                    )

                    if (promotion != null) {
                        call.respond(
                            status = HttpStatusCode.Created,
                            PromotionResponse("Promotion created successfully", promotion)
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.InternalServerError,
                            ErrorResponse("INTERNAL_ERROR", "Failed to create promotion")
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

        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Invalid ID")
            )

            try {
                val deletedCount = db.deletePromotionById(id)
                if (deletedCount != null && deletedCount > 0) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Promotion with ID $id deleted successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Failed to delete promotion: ${e.message}")
                )
            }
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Invalid ID")
            )

            try {
                val promotion = db.getPromotionById(id)
                if (promotion != null) {
                    call.respond(HttpStatusCode.OK, promotion)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Failed to retrieve promotion: ${e.message}")
                )
            }
        }

        // Update promotion - supports both JSON and multipart
        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Invalid or missing promotion ID")
            )

            val contentType = call.request.contentType()

            if (contentType.match(ContentType.MultiPart.FormData)) {
                // Handle multipart/form-data (with file upload)
                handlePromotionUpdateMultipart(call, db, id)
            } else {
                // Handle JSON
                try {
                    val request = call.receive<PromotionUpdateRequest>()
                    val existingPromotion = db.getPromotionById(id)
                        ?: return@put call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
                        )

                    val dateFormat = SimpleDateFormat("MM/dd/yyyy")
                    val startDate = request.startDate?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    } ?: existingPromotion.startDate
                    val endDate = request.endDate?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    } ?: existingPromotion.endDate

                    val result = db.updatePromo(
                        id = id,
                        userId = 1L, // Default admin user for global promotions
                        title = request.title ?: existingPromotion.title,
                        description = request.description ?: existingPromotion.description,
                        imageUrl = request.imageUrl ?: existingPromotion.imageUrl,
                        startDate = startDate,
                        endDate = endDate,
                        enabled = request.enabled ?: existingPromotion.enabled
                    )

                    if (result != null) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            SuccessResponse("Promotion updated successfully")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
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

        get {
            try {
                val promotions = db.getAllPromotions()
                if (promotions.isNullOrEmpty() == true ) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(
                            "NOT_FOUND",
                            "No Promotion Items Available inside the Database."
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.OK, promotions)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Failed to retrieve promotion: ${e.message}")
                )
            }
        }
    }
}

private suspend fun handlePromotionCreateMultipart(
    call: ApplicationCall,
    db: PromotionsRepositoryImpl
) {
    val multipart = call.receiveMultipart()
    var title: String? = null
    var description: String? = null
    var imageUrl: String? = null
    var startDate: Long? = null
    var endDate: Long? = null
    var enable: Boolean? = null
    val uploadDir = File("upload/products/promotions")
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
    }
    val dateFormat = SimpleDateFormat("MM/dd/yyyy")

    multipart.forEachPart { partData ->
        when (partData) {
            is PartData.FileItem -> {
                val fileName = partData.originalFileName?.replace(" ", "_")
                    ?: "name/${System.currentTimeMillis()}"
                val file = File(uploadDir, fileName)
                partData.streamProvider().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageUrl = "/upload/products/promotions/$fileName"
            }

            is PartData.FormItem -> {
                when (partData.name) {
                    "title" -> title = partData.value
                    "description" -> description = partData.value
                    "startDate" -> startDate = partData.value?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }

                    "endDate" -> endDate = partData.value?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }
                    "enable" -> enable = partData.value.toBooleanStrictOrNull()
                }
            }

            else -> {}
        }
    }
    try {
        val products = db.insertPromo(
            userId = 1L, // Default admin user for global promotions
            title = title ?: return call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Title Missing")
            ),
            description = description ?: return call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Description Missing")
            ),
            imageUrl = imageUrl ?: "/upload/products/promotions/default.jpg",
            startDate = startDate ?: return call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Start Date Missing")
            ),
            endDate = endDate ?: return call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "End Date Missing")
            ),
            enabled = enable ?: return call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Enabled Missing")
            )
        )
        products?.let {
            call.respond(HttpStatusCode.Created, it)
        } ?: call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse("INTERNAL_ERROR", "Failed to create promotion")
        )

    } catch (e: Exception) {
        call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse(
                "INTERNAL_ERROR",
                "Error While Uploading Promotions Products : ${e.message}"
            )
        )
    }
}

private suspend fun handlePromotionUpdateMultipart(
    call: ApplicationCall,
    db: PromotionsRepositoryImpl,
    id: Long
) {
    val multipart = call.receiveMultipart()
    var title: String? = null
    var description: String? = null
    var imageUrl: String? = null
    var startDate: Long? = null
    var endDate: Long? = null
    var enable: Boolean? = null
    val uploadDir = File("upload/products/promotions")
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
    }
    val dateFormat = SimpleDateFormat("MM/dd/yyyy")

    multipart.forEachPart { partData ->
        when (partData) {
            is PartData.FileItem -> {
                val fileName = partData.originalFileName?.replace(" ", "_")
                    ?: "name/${System.currentTimeMillis()}"
                val file = File(uploadDir, fileName)
                partData.streamProvider().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageUrl = "/upload/products/promotions/$fileName"
            }

            is PartData.FormItem -> {
                when (partData.name) {
                    "title" -> title = partData.value
                    "description" -> description = partData.value
                    "startDate" -> startDate = partData.value?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }

                    "endDate" -> endDate = partData.value?.let {
                        try {
                            it.toLongOrNull() ?: dateFormat.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }
                    "enable" -> enable = partData.value.toBooleanStrictOrNull()
                }
            }

            else -> {}
        }
    }

    try {
        // Get existing promotion to use as defaults for missing fields
        val existingPromotion = db.getPromotionById(id)
            ?: return call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
            )

        val services = db.updatePromo(
            id = id,
            userId = 1L, // Default admin user for global promotions
            title = title ?: existingPromotion.title,
            description = description ?: existingPromotion.description,
            imageUrl = imageUrl ?: existingPromotion.imageUrl,
            startDate = startDate ?: existingPromotion.startDate,
            endDate = endDate ?: existingPromotion.endDate,
            enabled = enable ?: existingPromotion.enabled
        )
        if (services != null) {
            call.respond(
                status = HttpStatusCode.OK,
                SuccessResponse("Promotion Updated Successfully")
            )
        } else {
            call.respond(
                status = HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", "Promotion with ID $id not found")
            )
        }

    } catch (e: Exception) {
        call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse("INTERNAL_ERROR", "Error While Updating Promotion: ${e.message}")
        )
    }
}