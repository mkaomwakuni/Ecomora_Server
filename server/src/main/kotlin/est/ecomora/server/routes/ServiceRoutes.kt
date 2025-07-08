package est.ecomora.server.routes

import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.serviceRoutes(
    db: EservicesRepositoryImpl
) {
    route("v1/services") {
        post {
            val threads = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var price: Long? = null
            var imageUrl: String? = null
            var category: String? = null
            var createdAt: String? = null
            var updatedAt: String? = null
            var offered: Long? = 0
            var isVisible: Boolean? = null
            val uploadsDirectory = File("upload/services/")
            if (!uploadsDirectory.exists()) {
                uploadsDirectory.mkdirs()
            }

            threads.forEachPart { segmentsData ->
                when(segmentsData) {
                    is PartData.FileItem -> {
                        val fileName = segmentsData.originalFileName?.replace(" ","_") ?: "Image${System.currentTimeMillis()}"
                        val file = File(uploadsDirectory, fileName)
                        segmentsData.streamProvider().use {input ->
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
                            "price" -> price = segmentsData.value.toLong()
                            "imageUrl" -> imageUrl = segmentsData.value
                            "category" -> category = segmentsData.value
                            "createdAt" -> createdAt = segmentsData.value
                            "updatedAt" -> updatedAt = segmentsData.value
                            "offered" -> offered = segmentsData.value.toLongOrNull()
                            "isVisible" -> isVisible = segmentsData.value.toBooleanStrictOrNull()
                        }
                    }
                    else -> {
                    }
                }
            }
            try {
                val service = db.insertService(
                        name ?: return@post call.respondText("Name Missing", status = HttpStatusCode.BadRequest),
                        description ?: return@post call.respondText("Description Missing", status = HttpStatusCode.BadRequest),
                        price ?: return@post call.respondText("Price Missing or Invalid", status = HttpStatusCode.BadRequest),
                    offered ?: 0,
                        category ?: return@post call.respondText("Category Missing", status = HttpStatusCode.BadRequest),
                    imageUrl ?: "/upload/services/default.jpg", // default image
                        isVisible ?: false,
                        createdAt ?: return@post call.respondText("Created Date Missing", status = HttpStatusCode.BadRequest),
                    updatedAt ?: return@post call.respondText(
                        "Updated Date Missing",
                        status = HttpStatusCode.BadRequest
                    )
                )
                service?.id?.let {
                    call.respond(
                        status = HttpStatusCode.Created,
                        "Service Created Successfully: $service"
                    )
                }
            }catch (e: Exception){
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Creating Service: ${e.message}"
                )
            }
        }

        get {
            try {
                val items = db.getAllServices()
                if (items?.isNotEmpty()==true){
                    call.respond(HttpStatusCode.OK, items)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        "No Items We Found"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Error Fetching Items ${e.message}"
                )
            }
        }

        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                text = "No ID Found...",
                status = HttpStatusCode.BadRequest
            )
            try {
                val products = db.getServiceById(id.toLong())
                if (products == null) {
                    call.respond(HttpStatusCode.BadRequest, "No Services Found")
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        products
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    "Error While Fetching Services ${e.message}"
                )
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                "Invalid Item ID"
            )
            try {
                val items = db.deleteServiceById(id.toLong())
                if (items == null) {
                    call.respond(
                        HttpStatusCode.OK,
                        "Item Deleted - Success"
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Item Not Found - Failure"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Error Deleting Items ${e.message}"
                )
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respondText(
                text = "Invalid or Missing Service ID",
                status = HttpStatusCode.BadRequest
            )
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var price: Long? = null
            var imageUrl: String? = null
            var category: String? = null
            var createdAt: String? = null
            var updatedDate: String? = null
            var offered: Long? = 0
            var isVisible: Boolean? = null

            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FileItem -> {
                        val fileName = partData.originalFileName?.replace(" ","_") ?: "Image${System.currentTimeMillis()}"
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
                            "category" -> category = partData.value
                            "createdAt" -> createdAt = partData.value
                            "updatedAt" -> updatedDate = partData.value
                            "offered" -> offered = partData.value.toLongOrNull()
                            "isVisible" -> isVisible = partData.value.toBooleanStrictOrNull()
                        }
                    }
                    else -> {
                    }
                }
                partData.dispose()
            }

            try {
                // Get existing service to use as defaults for missing fields
                val existingService = db.getServiceById(id)
                    ?: return@put call.respondText(
                        "Service with ID $id not found",
                        status = HttpStatusCode.NotFound
                    )

                val result = db.updateService(
                    id,
                    name ?: existingService.name,
                    description ?: existingService.description,
                    price ?: existingService.price,
                    offered ?: existingService.offered,
                    category ?: existingService.category,
                    imageUrl ?: existingService.imageUrl,
                    isVisible ?: existingService.isVisible,
                    createdAt ?: existingService.createdAt,
                    updatedDate ?: existingService.updatedAt
                )

                if (result != null && result > 0) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        "Service Updated Successfully"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Service with ID $id not found"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Updating Service: ${e.message}"
                )
            }
        }
    }
}