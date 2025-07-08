package est.ecomora.server.routes

import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.productRoutes(
    db: ProductsRepositoryImpl
) {
    route("v1/products") {
        post {
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
            var color: String? = null
            var sold: Long? = null
            var isFeatured: Boolean? = null
            var promotion: String? = null
            var productRating: Double? = null
            val uploadsDirectory = File("upload/products/")
            if (!uploadsDirectory.exists()) {
                uploadsDirectory.mkdirs()
            }

            threads.forEachPart { segmentsData ->
                when(segmentsData) {
                    is PartData.FileItem -> {
                        val fileName = segmentsData.originalFileName?.replace(" ","_") ?: "Image${System.currentTimeMillis()}"
                        val file = File("upload/products", fileName)
                        segmentsData.streamProvider().use {input ->
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
                            "productRating" -> productRating = segmentsData.value.toDoubleOrNull()
                            "color" -> color = segmentsData.value
                            "sold" -> sold = segmentsData.value.toLongOrNull()
                            "isFeatured" -> isFeatured = segmentsData.value.toBooleanStrictOrNull()
                        }
                    }
                    else -> {
                    }
                }
            }
            try {
                val product = db.insertProduct(
                    name ?: return@post call.respondText("Name Missing", status = HttpStatusCode.BadRequest),
                    description ?: return@post call.respondText("Description Missing", status = HttpStatusCode.BadRequest),
                    price ?: return@post call.respondText("Price Missing or Invalid", status = HttpStatusCode.BadRequest),
                    imageUrl ?: "/upload/products/default.jpg", // default image
                    categoryName ?: return@post call.respondText("Category Name Missing", status = HttpStatusCode.BadRequest),
                    categoryId ?: return@post call.respondText("Category ID Missing or Invalid", status = HttpStatusCode.BadRequest),
                    createdDate ?: System.currentTimeMillis().toString(), // default to current timestamp
                    updatedDate ?: System.currentTimeMillis().toString(), // default to current timestamp
                    totalStock ?: 0L, // default to 0
                    brand ?: "Unknown", // default brand
                    isAvailable ?: true, // default to available
                    discount ?: 0L, // default to no discount
                    promotion ?: "", // default to empty
                    productRating ?: 0.0, // default rating
                    color ?: "N/A", // default color
                    sold ?: 0L, // default to 0 sold
                    isFeatured ?: false // default to not featured
                )
                
                if (product?.id != null) {
                    call.respond(
                        status = HttpStatusCode.Created,
                        "Product Created Successfully: $product"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        "Failed to create product"
                    )
                }
            }catch (e: Exception){
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Creating Product: ${e.message}"
                )
            }
        }

        get("userId/{ids}") {
            val idsString = call.parameters["ids"]
            val ids = idsString?.removeSurrounding("[", "]")?.split(",")?.mapNotNull { it.toLongOrNull() }
            if (ids.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid IDs provided.")
                return@get
            }

            try {
                val products = db.getProductsByIds(ids)
                if (products?.isNotEmpty() == true) {
                    call.respond(HttpStatusCode.OK, products)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No products found with the provided IDs.")
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error while fetching products: ${e.message}"
                )
            }
        }

        get {
            try {
                val items = db.getAllProduct()
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
                val products = db.getProductById(id.toLong())
                if (products == null) {
                    call.respond(HttpStatusCode.BadRequest, "No Products Found")
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        products
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    "Error While Fetching Products ${e.message}"
                )
            }
        }

        get("multiple") {
            val idsString = call.request.queryParameters["ids"]
            if (idsString.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "No product IDs provided")
                return@get
            }

            val ids = idsString.split(",").mapNotNull { it.toLongOrNull() }
            if (ids.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product IDs provided")
                return@get
            }

            try {
                val products = db.getProductsByMultipleIds(ids)
                if (products.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No products found for the provided IDs")
                } else {
                    call.respond(HttpStatusCode.OK, products)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error while fetching products: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                "Invalid Item ID"
            )
            try {
                val items = db.deleteProductById(id.toLong())
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
                text = "Invalid or Missing Product ID",
                status = HttpStatusCode.BadRequest
            )
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
                            "isAvailable" -> isAvailable = partData.value.toBooleanStrictOrNull()
                            "discount" -> discount = partData.value.toLongOrNull()
                            "promotion" -> promotion = partData.value
                            "productRating" -> productRating = partData.value.toDoubleOrNull()
                            "color" -> color = partData.value
                            "isFeatured" -> isFeatured = partData.value.toBooleanStrictOrNull()
                        }
                    }

                    else -> {}
                }
            }
            val currentProduct = db.getProductById(id) ?: return@put call.respondText(
                text = "Product with ID $id not found",
                status = HttpStatusCode.NotFound
            )

            val updatedProduct = currentProduct.copy(
                name = name ?: currentProduct.name,
                description = description ?: currentProduct.description,
                price = price ?: currentProduct.price,
                categoryId = categoryId ?: currentProduct.categoryId,
                categoryName = categoryName ?: currentProduct.categoryName,
                imageUrl = imageUrl ?: currentProduct.imageUrl,
                createdDate = createdDate ?: currentProduct.createdDate,
                updatedDate = updatedDate ?: currentProduct.updatedDate,
                totalStock = totalStock ?: currentProduct.totalStock,
                brand = brand ?: currentProduct.brand,
                sold = currentProduct.sold,
                isAvailable = isAvailable ?: currentProduct.isAvailable,
                discount = discount ?: currentProduct.discount,
                promotion= promotion ?: currentProduct.promotion,
                isFeatured = isFeatured ?: currentProduct.isFeatured,
                color = color ?: currentProduct.color,
                productRating = productRating ?: currentProduct.productRating
            )

            try {
                val result = db.updateProductById(
                    id,
                    updatedProduct.name,
                    updatedProduct.description,
                    updatedProduct.price,
                    updatedProduct.imageUrl,
                    updatedProduct.categoryName,
                    updatedProduct.categoryId,
                    updatedProduct.createdDate,
                    updatedProduct.updatedDate,
                    updatedProduct.totalStock,
                    updatedProduct.brand,
                    updatedProduct.isAvailable,
                    updatedProduct.discount,
                    updatedProduct.sold,
                    updatedProduct.promotion,
                    updatedProduct.productRating,
                    updatedProduct.color,
                    updatedProduct.isFeatured,
                )

                if (result != null && result > 0) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        "Product Updated Successfully"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Product with ID $id not found"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    "Error While Updating Product: ${e.message}"
                )
            }
        }
    }
}