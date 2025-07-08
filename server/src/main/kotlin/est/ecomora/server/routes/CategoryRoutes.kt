package est.ecomora.server.routes

import est.ecomora.server.data.local.table.category.CategoriesTable.productCount
import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.categoryRoutes(
    db: CategoriesRepositoryImpl
) {
    route("v1/categories") {
        post {
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var isVisible: Boolean? = null
            var imageUrl: String? = null
            val uploadDir = File("upload/products/categories")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "description" -> description = part.value
                            "isVisible" -> isVisible = part.value.toBooleanStrictOrNull()
                        }
                    }

                    is PartData.FileItem -> {
                        val fileName = part.originalFileName?.replace(" ", "_")
                            ?: "image${System.currentTimeMillis()}"
                        val file = File(uploadDir, fileName)
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/categories/${fileName}"
                    }

                    else -> {}
                }
            }

            name ?: return@post call.respond(
                status = HttpStatusCode.BadRequest,
                "Name Missing"
            )
            description ?: return@post call.respond(
                status = HttpStatusCode.BadRequest,
                "Description Missing"
            )
            isVisible ?: return@post call.respond(
                status = HttpStatusCode.BadRequest,
                "isVisible Missing"
            )

            // Provide default imageUrl if no image was uploaded
            val finalImageUrl = imageUrl ?: "/upload/products/categories/default-category.png"

            try {
                val category = db.insertCategory(name!!, description!!, isVisible!!, finalImageUrl)
                category?.id.let { categoryId ->
                    call.respond(
                        status = HttpStatusCode.OK,
                        "Category Uploaded Successfully to Server : $category"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Error While Uploading Category To Server : ${e.message}"
                )
            }
        }

        get {
            try {
                val category = db.getAllCategories()
                if (category?.isNotEmpty() == true) {
                    call.respond(
                        HttpStatusCode.OK,
                        category
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "No Category Found"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Error While Fetching  ${e.message}"
                )
            }
        }

        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                "Invalid Category"
            )
            try {
                val categoryId = id.toLong()
                val categories = db.getCategoryById(categoryId)
                if (categories == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "No Category Found..."
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        categories
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Error Fetching the Categories ${e.message}"
                )
            }
        }

        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                "Error Occurred: ID Missing"
            )
            try {
                val category = db.deleteCategory(id.toLong())
                if (category == 1) {
                    call.respond(
                        HttpStatusCode.OK,
                        "Category Deleted"
                    )
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Id Not Found...")
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Error Fetching Categories ${e.message}"
                )
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toLong() ?: return@put call.respondText(
                text = "Invalid Id",
                status = HttpStatusCode.BadRequest
            )
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var isVisible: Boolean? = null
            var imageUrl: String? = null
            val uploadDir = File("upload/products/categories")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "description" -> description = part.value
                            "isVisible" -> isVisible = part.value.toBooleanStrictOrNull()
                        }
                    }

                    is PartData.FileItem -> {
                        val fileName = part.originalFileName?.replace(" ", "_")
                            ?: "image${System.currentTimeMillis()}"
                        val file = File(uploadDir, fileName)
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/categories/${fileName}"
                    }

                    else -> {}
                }
            }
            if (name == null && description == null && isVisible == null && imageUrl == null) {
                return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    "No fields to update"
                )
            }

            try {
                val existingCategory = db.getCategoryById(id) ?: return@put call.respond(
                    status = HttpStatusCode.NotFound,
                    "Category not found"
                )

                val updatedName = name ?: existingCategory.name
                val updatedDescription = description ?: existingCategory.description
                val updatedIsVisible = isVisible ?: existingCategory.isVisible
                val updatedImageUrl = imageUrl ?: existingCategory.imageUrl

                val result = db.updateCategory(
                    id,
                    updatedName,
                    updatedDescription,
                    updatedIsVisible,
                    updatedImageUrl
                )
                if (result == 1) {
                    call.respond(HttpStatusCode.OK, "Update Successfully $result")
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Error Occurred"
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    "Error Updating Category: ${e.message}"
                )
            }
        }
    }
}