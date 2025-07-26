package est.ecomora.server.routes

import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.plugins.getCurrentUserId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import est.ecomora.server.routes.ErrorResponse
import est.ecomora.server.routes.SuccessResponse

@Serializable
data class ProductCreateRequest(
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String? = null,
    val categoryName: String,
    val categoryId: Long,
    val createdDate: String? = null,
    val updatedDate: String? = null,
    val totalStock: Long = 0,
    val brand: String = "Unknown",
    val isAvailable: Boolean = true,
    val discount: Long = 0,
    val color: String = "N/A",
    val sold: Long = 0,
    val isFeatured: Boolean = false,
    val promotion: String = "",
    val productRating: Double = 0.0
)

@Serializable
data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val imageUrl: String? = null,
    val categoryName: String? = null,
    val categoryId: Long? = null,
    val createdDate: String? = null,
    val updatedDate: String? = null,
    val totalStock: Long? = null,
    val brand: String? = null,
    val isAvailable: Boolean? = null,
    val discount: Long? = null,
    val color: String? = null,
    val sold: Long? = null,
    val isFeatured: Boolean? = null,
    val promotion: String? = null,
    val productRating: Double? = null
)

fun Route.productRoutes(
    db: ProductsRepositoryImpl
) {
    route("v1/products") {
        authenticate("auth-jwt") {
            post {
                val userId = call.getCurrentUserId() ?: return@post call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val contentType = call.request.contentType()
                if (contentType.match(ContentType.MultiPart.FormData)) {
                    val threads = call.receiveMultipart()
                    var name: String? = null
                    var description: String? = null
                    var price: Long? = null
                    var imageUrl: String? = null
                    var categoryName: String? = null
                    var categoryId: Long? = null
                    var createdDate: String? = null
                    var updatedDate: String? = null
                    var totalStock: Long? = null
                    var brand: String? = null
                    var isAvailable: Boolean? = null
                    var discount: Long? = null
                    var promotion: String? = null
                    var productRating: Double? = null
                    var color: String? = null
                    var sold: Long? = null
                    var isFeatured: Boolean? = null
                    val uploadsDirectory = File("upload/products/")
                    if (!uploadsDirectory.exists()) {
                        uploadsDirectory.mkdirs()
                    }

                    threads.forEachPart { segmentsData ->
                        when (segmentsData) {
                            is PartData.FileItem -> {
                                val fileName = segmentsData.originalFileName?.replace(" ", "_")
                                    ?: "Image${System.currentTimeMillis()}"
                                val file = File("upload/products", fileName)
                                segmentsData.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                imageUrl = "/upload/products/$fileName"
                            }
                            is PartData.FormItem -> {
                                when (segmentsData.name) {
                                    "name" -> name = segmentsData.value
                                    "description" -> description = segmentsData.value
                                    "price" -> price = segmentsData.value.toLong()
                                    "imageUrl" -> imageUrl = segmentsData.value
                                    "categoryName" -> categoryName = segmentsData.value
                                    "categoryId" -> categoryId = segmentsData.value.toLongOrNull()
                                    "createdDate" -> createdDate = segmentsData.value
                                    "updatedDate" -> updatedDate = segmentsData.value
                                    "totalStock" -> totalStock = segmentsData.value.toLongOrNull()
                                    "brand" -> brand = segmentsData.value
                                    "isAvailable" -> isAvailable =
                                        segmentsData.value.toBooleanStrictOrNull()

                                    "discount" -> discount = segmentsData.value.toLongOrNull()
                                    "promotion" -> promotion = segmentsData.value
                                    "productRating" -> productRating =
                                        segmentsData.value.toDoubleOrNull()

                                    "color" -> color = segmentsData.value
                                    "sold" -> sold = segmentsData.value.toLongOrNull()
                                    "isFeatured" -> isFeatured =
                                        segmentsData.value.toBooleanStrictOrNull()
                                }
                            }
                            else -> {
                            }
                        }
                    }
                    try {
                        val product = db.insertProduct(
                            name ?: return@post call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse("BAD_REQUEST", "Name Missing")
                            ),
                            description ?: return@post call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse("BAD_REQUEST", "Description Missing")
                            ),
                            price ?: return@post call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse("BAD_REQUEST", "Price Missing or Invalid")
                            ),
                            imageUrl ?: "/upload/products/default.jpg", // default image
                            categoryName ?: return@post call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse("BAD_REQUEST", "Category Name Missing")
                            ),
                            categoryId ?: return@post call.respond(
                                status = HttpStatusCode.BadRequest,
                                ErrorResponse("BAD_REQUEST", "Category ID Missing or Invalid")
                            ),
                            createdDate ?: System.currentTimeMillis()
                                .toString(), // default to current timestamp
                            updatedDate ?: System.currentTimeMillis()
                                .toString(), // default to current timestamp
                            totalStock ?: 0L, // default to 0
                            brand ?: "Unknown", // default brand
                            isAvailable ?: true, // default to available
                            discount ?: 0L, // default to no discount
                            promotion ?: "", // default to empty
                            productRating ?: 0.0, // default rating
                            color ?: "N/A", // default color
                            sold ?: 0L, // default to 0 sold
                            isFeatured ?: false, // default to not featured
                            userId
                        )

                        if (product?.id != null) {
                            call.respond(
                                status = HttpStatusCode.Created,
                                product
                            )
                        } else {
                            call.respond(
                                status = HttpStatusCode.InternalServerError,
                                ErrorResponse("INTERNAL_ERROR", "Failed to create product")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            status = HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                "INTERNAL_ERROR",
                                "Error While Creating Product: ${e.message}"
                            )
                        )
                    }
                } else if (contentType.match(ContentType.Application.Json)) {
                    val productCreateRequest = call.receive<ProductCreateRequest>()
                    try {
                        val product = db.insertProduct(
                            productCreateRequest.name,
                            productCreateRequest.description,
                            productCreateRequest.price,
                            productCreateRequest.imageUrl ?: "/upload/products/default.jpg",
                            productCreateRequest.categoryName,
                            productCreateRequest.categoryId,
                            productCreateRequest.createdDate ?: System.currentTimeMillis()
                                .toString(), // default to current timestamp
                            productCreateRequest.updatedDate ?: System.currentTimeMillis()
                                .toString(), // default to current timestamp
                            productCreateRequest.totalStock,
                            productCreateRequest.brand,
                            productCreateRequest.isAvailable,
                            productCreateRequest.discount,
                            productCreateRequest.promotion,
                            productCreateRequest.productRating,
                            productCreateRequest.color,
                            productCreateRequest.sold,
                            productCreateRequest.isFeatured,
                            userId
                        )

                        if (product?.id != null) {
                            call.respond(
                                status = HttpStatusCode.Created,
                                product
                            )
                        } else {
                            call.respond(
                                status = HttpStatusCode.InternalServerError,
                                ErrorResponse("INTERNAL_ERROR", "Failed to create product")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            status = HttpStatusCode.InternalServerError,
                            ErrorResponse(
                                "INTERNAL_ERROR",
                                "Error While Creating Product: ${e.message}"
                            )
                        )
                    }
                } else {
                    call.respond(
                        status = HttpStatusCode.UnsupportedMediaType,
                        ErrorResponse("UNSUPPORTED_MEDIA_TYPE", "Unsupported media type")
                    )
                }
            }

            get {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                try {
                    val items = db.getAllProductsByUserId(userId)
                    if (items?.isNotEmpty() == true) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            items
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No Items We Found")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
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
                    val product = db.getProductById(id.toLong(), userId)
                    if (product == null) {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No Products Found")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            product
                        )
                    }

                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            "INTERNAL_ERROR",
                            "Error While Fetching Products ${e.message}"
                        )
                    )
                }
            }

            get("userId/{ids}") {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val idsString = call.parameters["ids"]
                val ids = idsString?.removeSurrounding("[", "]")?.split(",")
                    ?.mapNotNull { it.toLongOrNull() }
                if (ids.isNullOrEmpty()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse("BAD_REQUEST", "Invalid IDs provided.")
                    )
                    return@get
                }

                try {
                    val products = db.getProductsByIds(ids, userId)
                    if (products?.isNotEmpty() == true) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            products
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No products found with the provided IDs.")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            "INTERNAL_ERROR",
                            "Error while fetching products: ${e.message}"
                        )
                    )
                }
            }

            get("multiple") {
                val userId = call.getCurrentUserId() ?: return@get call.respond(
                    status = HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "User not authenticated")
                )

                val idsString = call.request.queryParameters["ids"]
                if (idsString.isNullOrBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse("BAD_REQUEST", "No product IDs provided")
                    )
                    return@get
                }

                val ids = idsString.split(",").mapNotNull { it.toLongOrNull() }
                if (ids.isEmpty()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse("BAD_REQUEST", "Invalid product IDs provided")
                    )
                    return@get
                }

                try {
                    val products = db.getProductsByMultipleIds(ids, userId)
                    if (products.isNullOrEmpty()) {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "No products found for the provided IDs")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            products
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            "INTERNAL_ERROR",
                            "Error while fetching products: ${e.message}"
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
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse("BAD_REQUEST", "Invalid Item ID")
                )
                try {
                    val items = db.deleteProductById(id.toLong(), userId)
                    if (items == 1) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            SuccessResponse("Item Deleted - Success")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Item Not Found - Failure")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
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
                    ErrorResponse("BAD_REQUEST", "Invalid or Missing Product ID")
                )
                val contentType = call.request.contentType()
                if (contentType.match(ContentType.MultiPart.FormData)) {
                    val multipart = call.receiveMultipart()
                    var name: String? = null
                    var description: String? = null
                    var price: Long? = null
                    var imageUrl: String? = null
                    var categoryName: String? = null
                    var categoryId: Long? = null
                    var createdDate: String? = null
                    var updatedDate: String? = null
                    var totalStock: Long? = null
                    var brand: String? = null
                    var isAvailable: Boolean? = null
                    var discount: Long? = null
                    var promotion: String? = null
                    var productRating: Double? = null
                    var isFeatured: Boolean? = null
                    var color: String? = null

                    multipart.forEachPart { partData ->
                        when (partData) {
                            is PartData.FileItem -> {
                                val fileName = partData.originalFileName?.replace(" ", "_")
                                    ?: "Image${System.currentTimeMillis()}"
                                val file = File("upload/products", fileName)
                                partData.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                imageUrl = "/upload/products/$fileName"
                            }

                            is PartData.FormItem -> {
                                when (partData.name) {
                                    "name" -> name = partData.value
                                    "description" -> description = partData.value
                                    "price" -> price = partData.value.toLongOrNull()
                                    "imageUrl" -> imageUrl = partData.value
                                    "categoryName" -> categoryName = partData.value
                                    "categoryId" -> categoryId = partData.value.toLongOrNull()
                                    "createdDate" -> createdDate = partData.value
                                    "updatedDate" -> updatedDate = partData.value
                                    "totalStock" -> totalStock = partData.value.toLongOrNull()
                                    "brand" -> brand = partData.value
                                    "isAvailable" -> isAvailable =
                                        partData.value.toBooleanStrictOrNull()

                                    "discount" -> discount = partData.value.toLongOrNull()
                                    "promotion" -> promotion = partData.value
                                    "productRating" -> productRating =
                                        partData.value.toDoubleOrNull()

                                    "color" -> color = partData.value
                                    "isFeatured" -> isFeatured =
                                        partData.value.toBooleanStrictOrNull()
                                }
                            }

                            else -> {}
                        }
                    }
                    val currentProduct = db.getProductById(id, userId) ?: return@put call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Product with ID $id not found")
                    )

                    try {
                        val result = db.updateProductById(
                            id,
                            name ?: currentProduct.name,
                            description ?: currentProduct.description,
                            price ?: currentProduct.price,
                            imageUrl ?: currentProduct.imageUrl,
                            categoryName ?: currentProduct.categoryName,
                            categoryId ?: currentProduct.categoryId,
                            createdDate ?: currentProduct.createdDate,
                            updatedDate ?: currentProduct.updatedDate,
                            totalStock ?: currentProduct.totalStock,
                            brand ?: currentProduct.brand,
                            isAvailable ?: currentProduct.isAvailable,
                            discount ?: currentProduct.discount,
                            currentProduct.sold,
                            promotion ?: currentProduct.promotion,
                            productRating ?: currentProduct.productRating,
                            color ?: currentProduct.color,
                            isFeatured ?: currentProduct.isFeatured,
                            userId
                        )

                        if (result != null && result > 0) {
                            val updatedProduct = db.getProductById(id, userId)
                            if (updatedProduct != null) {
                                call.respond(
                                    status = HttpStatusCode.OK,
                                    updatedProduct
                                )
                            } else {
                                call.respond(
                                    status = HttpStatusCode.InternalServerError,
                                    ErrorResponse("INTERNAL_ERROR", "Failed to retrieve updated product")
                                )
                            }
                        } else {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                ErrorResponse("NOT_FOUND", "Product with ID $id not found")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            ErrorResponse(
                                "BAD_REQUEST",
                                "Error While Updating Product: ${e.message}"
                            )
                        )
                    }
                } else if (contentType.match(ContentType.Application.Json)) {
                    val productUpdateRequest = call.receive<ProductUpdateRequest>()
                    val currentProduct = db.getProductById(id, userId) ?: return@put call.respond(
                        status = HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Product with ID $id not found")
                    )

                    try {
                        val result = db.updateProductById(
                            id,
                            productUpdateRequest.name ?: currentProduct.name,
                            productUpdateRequest.description ?: currentProduct.description,
                            productUpdateRequest.price ?: currentProduct.price,
                            productUpdateRequest.imageUrl ?: currentProduct.imageUrl,
                            productUpdateRequest.categoryName ?: currentProduct.categoryName,
                            productUpdateRequest.categoryId ?: currentProduct.categoryId,
                            productUpdateRequest.createdDate ?: currentProduct.createdDate,
                            productUpdateRequest.updatedDate ?: currentProduct.updatedDate,
                            productUpdateRequest.totalStock ?: currentProduct.totalStock,
                            productUpdateRequest.brand ?: currentProduct.brand,
                            productUpdateRequest.isAvailable ?: currentProduct.isAvailable,
                            productUpdateRequest.discount ?: currentProduct.discount,
                            currentProduct.sold,
                            productUpdateRequest.promotion ?: currentProduct.promotion,
                            productUpdateRequest.productRating ?: currentProduct.productRating,
                            productUpdateRequest.color ?: currentProduct.color,
                            productUpdateRequest.isFeatured ?: currentProduct.isFeatured,
                            userId
                        )

                        if (result != null && result > 0) {
                            val updatedProduct = db.getProductById(id, userId)
                            if (updatedProduct != null) {
                                call.respond(
                                    status = HttpStatusCode.OK,
                                    updatedProduct
                                )
                            } else {
                                call.respond(
                                    status = HttpStatusCode.InternalServerError,
                                    ErrorResponse("INTERNAL_ERROR", "Failed to retrieve updated product")
                                )
                            }
                        } else {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                ErrorResponse("NOT_FOUND", "Product with ID $id not found")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            ErrorResponse(
                                "BAD_REQUEST",
                                "Error While Updating Product: ${e.message}"
                            )
                        )
                    }
                } else {
                    call.respond(
                        status = HttpStatusCode.UnsupportedMediaType,
                        ErrorResponse("UNSUPPORTED_MEDIA_TYPE", "Unsupported media type")
                    )
                }
            }
        }
    }
}