package est.ecomora.server.routes

import est.ecomora.server.domain.model.prints.PrintsRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.printRoutes(
    db: PrintsRepositoryImpl
) {
    route("v1/prints") {
        post {
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var price: Double? = null
            var imageUrl: String? = null
            var copies: Int? = null
            val uploadDir = File("upload/products/prints/")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FileItem -> {
                        val fileName = partData.originalFileName?.replace(" ", "_")
                            ?: "image${System.currentTimeMillis()}"
                        val file = File(uploadDir, fileName)
                        partData.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/prints/$fileName"
                    }

                    is PartData.FormItem -> {
                        when (partData.name) {
                            "name" -> name = partData.value
                            "description" -> description = partData.value
                            "price" -> price = partData.value.toDoubleOrNull()
                            "copies" -> copies = partData.value.toIntOrNull()
                        }
                    }

                    else -> {}
                }
                partData.dispose()
            }

            try {
                val print = db.insertPrint(
                    name ?: return@post call.respondText(
                        "Name Missing",
                        status = HttpStatusCode.BadRequest
                    ),
                    description ?: return@post call.respondText(
                        "Description Missing",
                        status = HttpStatusCode.BadRequest
                    ),
                    price ?: return@post call.respondText(
                        "Price Missing or Invalid",
                        status = HttpStatusCode.BadRequest
                    ),
                    imageUrl ?: "/upload/products/prints/default.jpg",
                    copies ?: return@post call.respondText(
                        "Copies Missing or Invalid",
                        status = HttpStatusCode.BadRequest
                    )
                )
                print?.id?.let {
                    call.respond(
                        status = HttpStatusCode.Created,
                        "Print Created Successfully: $print"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Creating Print: ${e.message}"
                )
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var price: Double? = null
            var imageUrl: String? = null
            var copies: Int? = null
            val uploadDir = File("upload/products/prints/")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FormItem -> {
                        when (partData.name) {
                            "name" -> name = partData.value
                            "description" -> description = partData.value
                            "price" -> price = partData.value.toDoubleOrNull()
                            "copies" -> copies = partData.value.toIntOrNull()
                        }
                    }

                    is PartData.FileItem -> {
                        val fileName = partData.originalFileName?.replace(" ", "_")
                            ?: "image${System.currentTimeMillis()}"
                        val file = File(uploadDir, fileName)
                        partData.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/prints/$fileName"
                    }

                    else -> {}
                }
                partData.dispose()
            }

            try {
                // Get existing print to use as defaults for missing fields
                val existingPrint = db.getPrintById(id)
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        "Print with ID $id not found"
                    )

                val updatedCount = db.updatePrint(
                    id = id,
                    name = name ?: existingPrint.name,
                    description = description ?: existingPrint.description,
                    price = price ?: existingPrint.price,
                    imageUrl = imageUrl ?: existingPrint.imageUrl,
                    copies = copies ?: existingPrint.copies
                )
                if (updatedCount != null && updatedCount > 0) {
                    call.respond(HttpStatusCode.OK, "Print with ID $id updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Print with ID $id not found")
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to update print: ${e.message}"
                )
            }
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            try {
                val print = db.getPrintById(id)
                if (print != null) {
                    call.respond(HttpStatusCode.OK, print)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Print with ID $id not found")
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to retrieve print: ${e.message}"
                )
            }
        }

        get {
            try {
                val prints = db.getAllPrints()
                if (prints.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No prints found")
                } else {
                    call.respond(HttpStatusCode.OK, prints)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to retrieve prints: ${e.message}"
                )
            }
        }
    }
}