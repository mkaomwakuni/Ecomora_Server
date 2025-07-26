package est.ecomora.server.routes

import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import est.ecomora.server.domain.model.category.CategoryType
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class CategoryCreateRequest(
    val name: String,
    val description: String,
    val isVisible: Boolean,
    val imageUrl: String? = null,
    val categoryType: String = "PRODUCT"
)

@Serializable
data class CategoryUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val isVisible: Boolean? = null,
    val imageUrl: String? = null,
    val categoryType: String? = null
)

@Serializable
data class CategoryResponse(
    val message: String,
    val category: est.ecomora.server.domain.model.category.Categories
)

fun Route.categoryRoutes(
    db: CategoriesRepositoryImpl
) {
    route("v1/categories") {
        // Create category - supports both JSON and multipart
        post {
            val contentType = call.request.contentType()

            if (contentType.match(ContentType.MultiPart.FormData)) {
                // Handle multipart/form-data (with optional file upload)
                handleCategoryCreateMultipart(call, db)
            } else {
                // Handle JSON
                try {
                    val request = call.receive<CategoryCreateRequest>()

                    // Validate categoryType
                    val categoryType = try {
                        CategoryType.valueOf(request.categoryType.uppercase())
                    } catch (e: IllegalArgumentException) {
                        return@post call.respond(
                            status = HttpStatusCode.BadRequest,
                            ErrorResponse(
                                "INVALID_CATEGORY_TYPE",
                                "Invalid category type. Must be PRODUCT or SERVICE"
                            )
                        )
                    }

                    // Check if category with this name already exists
                    val existingCategory = db.getCategoryByName(request.name)
                    if (existingCategory != null) {
                        return@post call.respond(
                            status = HttpStatusCode.Conflict,
                            ErrorResponse(
                                "DUPLICATE_NAME",
                                "Category with name '${request.name}' already exists. Use a different name or update the existing category."
                            )
                        )
                    }

                    val category = db.insertCategory(
                        request.name,
                        request.description,
                        request.isVisible,
                        request.imageUrl, // Allow null values
                        categoryType
                    )

                    if (category?.id != null) {
                        call.respond(
                            status = HttpStatusCode.Created,
                            CategoryResponse("Category created successfully", category)
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.InternalServerError,
                            ErrorResponse("INTERNAL_ERROR", "Failed to create category")
                        )
                    }
                } catch (e: ContentTransformationException) {
                    // Handle JSON parsing errors more specifically
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "INVALID_JSON",
                            "Invalid JSON format. Expected: {\"name\":\"string\", \"description\":\"string\", \"isVisible\":boolean, \"imageUrl\":\"string or null\", \"categoryType\":\"PRODUCT or SERVICE\"}"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "BAD_REQUEST",
                            "Invalid request format or missing required fields: ${e.message}"
                        )
                    )
                }
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
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "No categories found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error while fetching categories: ${e.message}")
                )
            }
        }

        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Category ID missing")
            )
            try {
                val categoryId = id.toLong()
                val categories = db.getCategoryById(categoryId)
                if (categories == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Category not found")
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
                    ErrorResponse("BAD_REQUEST", "Error fetching category: ${e.message}")
                )
            }
        }

        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Category ID missing")
            )
            try {
                val category = db.deleteCategory(id.toLong())
                if (category == 1) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Category deleted successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Category not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "Error deleting category: ${e.message}")
                )
            }
        }

        // Update category - supports both JSON and multipart
        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", "Invalid or missing category ID")
            )

            val contentType = call.request.contentType()

            if (contentType.match(ContentType.MultiPart.FormData)) {
                // Handle multipart/form-data (with optional file upload)
                handleCategoryUpdateMultipart(call, db, id)
            } else {
                // Handle JSON
                try {
                    val request = call.receive<CategoryUpdateRequest>()
                    val existingCategory = db.getCategoryById(id) ?: return@put call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Category not found")
                    )

                    // Validate categoryType if provided
                    val categoryType = if (request.categoryType != null) {
                        try {
                            CategoryType.valueOf(request.categoryType.uppercase())
                        } catch (e: IllegalArgumentException) {
                            return@put call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse(
                                    "INVALID_CATEGORY_TYPE",
                                    "Invalid category type. Must be PRODUCT or SERVICE"
                                )
                            )
                        }
                    } else {
                        existingCategory.categoryType
                    }

                    val result = db.updateCategory(
                        id,
                        request.name ?: existingCategory.name,
                        request.description ?: existingCategory.description,
                        request.isVisible ?: existingCategory.isVisible,
                        request.imageUrl ?: existingCategory.imageUrl,
                        categoryType
                    )

                    if (result == 1) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse("Category updated successfully")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Category not found")
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

private suspend fun handleCategoryCreateMultipart(
    call: io.ktor.server.application.ApplicationCall,
    db: CategoriesRepositoryImpl
) {
    val multipart = call.receiveMultipart()
    var name: String? = null
    var description: String? = null
    var isVisible: Boolean? = null
    var imageUrl: String? = null
    var categoryType: CategoryType = CategoryType.PRODUCT

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
                    "categoryType" -> {
                        categoryType = try {
                            CategoryType.valueOf(part.value.uppercase())
                        } catch (e: IllegalArgumentException) {
                            CategoryType.PRODUCT
                        }
                    }
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
        part.dispose()
    }

    // Validate required fields
    name ?: return call.respond(
        status = HttpStatusCode.BadRequest,
        ErrorResponse("BAD_REQUEST", "Name is required")
    )
    description ?: return call.respond(
        status = HttpStatusCode.BadRequest,
        ErrorResponse("BAD_REQUEST", "Description is required")
    )
    isVisible ?: return call.respond(
        status = HttpStatusCode.BadRequest,
        ErrorResponse("BAD_REQUEST", "isVisible is required")
    )

    try {
        // Check if category with this name already exists
        val existingCategory = db.getCategoryByName(name)
        if (existingCategory != null) {
            return call.respond(
                status = HttpStatusCode.Conflict,
                ErrorResponse(
                    "DUPLICATE_NAME",
                    "Category with name '$name' already exists. Use a different name or update the existing category."
                )
            )
        }

        val category = db.insertCategory(
            name!!,
            description!!,
            isVisible!!,
            imageUrl, // Allow null values
            categoryType
        )

        if (category?.id != null) {
            call.respond(
                status = HttpStatusCode.Created,
                CategoryResponse("Category created successfully", category)
            )
        } else {
            call.respond(
                status = HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", "Failed to create category")
            )
        }
    } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse("INTERNAL_ERROR", "Error creating category: ${e.message}")
        )
    }
}

private suspend fun handleCategoryUpdateMultipart(
    call: io.ktor.server.application.ApplicationCall,
    db: CategoriesRepositoryImpl,
    id: Long
) {
    val multipart = call.receiveMultipart()
    var name: String? = null
    var description: String? = null
    var isVisible: Boolean? = null
    var imageUrl: String? = null
    var categoryType: CategoryType? = null

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
                    "categoryType" -> {
                        categoryType = try {
                            CategoryType.valueOf(part.value.uppercase())
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
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
        part.dispose()
    }

    if (name == null && description == null && isVisible == null && imageUrl == null && categoryType == null) {
        return call.respond(
            status = HttpStatusCode.BadRequest,
            ErrorResponse("BAD_REQUEST", "No fields to update")
        )
    }

    try {
        val existingCategory = db.getCategoryById(id) ?: return call.respond(
            status = HttpStatusCode.NotFound,
            ErrorResponse("NOT_FOUND", "Category not found")
        )

        // Check if category with this name already exists
        if (name != null) {
            val existingCategoryWithSameName = db.getCategoryByName(name)
            if (existingCategoryWithSameName != null && existingCategoryWithSameName.id != id) {
                return call.respond(
                    status = HttpStatusCode.Conflict,
                    ErrorResponse(
                        "DUPLICATE_NAME",
                        "Category with name '$name' already exists. Use a different name or update the existing category."
                    )
                )
            }
        }

        val result = db.updateCategory(
            id,
            name ?: existingCategory.name,
            description ?: existingCategory.description,
            isVisible ?: existingCategory.isVisible,
            imageUrl ?: existingCategory.imageUrl,
            categoryType ?: existingCategory.categoryType
        )

        if (result == 1) {
            call.respond(
                HttpStatusCode.OK,
                SuccessResponse("Category updated successfully")
            )
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("NOT_FOUND", "Category not found")
            )
        }
    } catch (e: Exception) {
        call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse("INTERNAL_ERROR", "Error updating category: ${e.message}")
        )
    }
}