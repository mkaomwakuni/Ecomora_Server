package est.tunzo.cyberpros.server.plugins

import est.tunzo.cyberpros.server.domain.repository.category.CategoriesRepositoryImpl
import est.tunzo.cyberpros.server.domain.repository.products.ProductsRepositoryImpl
import est.tunzo.cyberpros.server.domain.repository.users.UsersRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.datetime.Clock
import sun.jvm.hotspot.HelloWorld.e
import java.io.File
import java.nio.file.Files
/**
 * Provides utility for working with file system paths in Kotlin.
 *
 * This import allows for creating, manipulating, and resolving file system paths
 * using the java.nio.file.Paths utility class.
 */
import java.nio.file.Paths

/**
 * Handles user registration route for creating new users.
 *
 * This function processes user registration by:
 * - Receiving user details via HTTP POST parameters
 * - Validating required fields (username, email, password, fullName, phoneNumber, userRole)
 * - Inserting the new user into the database
 * - Responding with success or error status
 *
 * @param db The users repository used for database operations
 * @throws HttpStatusCode.Unauthorized if any required parameter is missing
 * @throws HttpStatusCode.BadRequest if user creation fails
 */
fun Route.users(
    db: UsersRepository
) {
    post("v1/users") {
        val parameters = call.receive<Parameters>()
        val username = parameters["username"] ?: return@post call.respondText(
            text = " Username is Missing, Please provide a username",
            status = io.ktor.http.HttpStatusCode.Unauthorized
        )

        val email = parameters["email"] ?: return@post call.respondText(
            text = " Email is Missing, Please provide an email",
            status = io.ktor.http.HttpStatusCode.Unauthorized
        )
        val password = parameters["password"] ?: return@post call.respondText(
            text = " Password is Missing, Please provide a password",
            status = HttpStatusCode.Unauthorized
        )
        val fullName = parameters["fullName"] ?: return@post call.respondText(
            text = "fullName Missing",
            status = HttpStatusCode.Unauthorized
        )
        val phoneNumber = parameters["phoneNumber"] ?: return@post call.respondText(
            text = "phoneNumber Missing",
            status = HttpStatusCode.Unauthorized
        )
        val userRole = parameters["country"] ?: return@post call.respondText(
            text = "User Role Missing",
            status = HttpStatusCode.Unauthorized
        )

        try {
            val users = db.insertUser(
                username,
                password,
                email,
                phoneNumber,
                userRole
            )
            users?.let {
                call.respondText(
                    status = HttpStatusCode.OK,
                    text = "User Successfully Created"
                )
            }
        } catch (e: Exception) {
            call.respondText(
                status = HttpStatusCode.BadRequest,
                text = "Error Creating User"
            )
        }
    }

    get("v1/users") {
        try {
            val users = db.getAllUsers()
            if (users?.isNotEmpty() == true) {
                call.respond(HttpStatusCode.OK, users)
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                "Error While Fetching the Data: ${e.message}"
            )
        }
    }
    get("v1/users/{id}") {
        val parameters = call.parameters["id"]
        try {
            val usersId = parameters?.toLong()
            if (usersId == null) {
                return@get call.respondText(
                    "Invalid ID",
                    status = HttpStatusCode.BadRequest
                )
            }
            val users = db.getUsersById(usersId)
            if (users == null) {
                return@get call.respondText(
                    text = "User Not Found",
                    status = HttpStatusCode.NotFound
                )
            } else {
                return@get call.respond(
                    HttpStatusCode.OK,
                    users
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                "Error Fetching from the server ${e.message}"
            )
        }
    }
    delete("v1/users/{id}") {
        val parameters = call.parameters["id"]
        try {
            val users = parameters?.toLongOrNull()?.let { usersId ->
                db.deleteUserById(usersId)
            } ?: return@delete call.respondText(
                "No Id Found",
                status = HttpStatusCode.BadRequest
            )

            if (users == 1) {
                call.respondText(
                    "Deleted User Successfully",
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    "ID Not Found",
                    status = HttpStatusCode.BadRequest
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                "Error From The Server Deleting User: ${e.message}"
            )
        }
    }
    put("v1/users/{id}") {
        val id = call.parameters["id"] ?: return@put call.respondText(
            text = "Id Not Found",
            status = HttpStatusCode.NotFound
        )
        val parameters = call.receive<Parameters>()
        val username = parameters["username"] ?: return@put call.respondText(
            text = "Username Missing",
            status = HttpStatusCode.BadRequest
        )
        val email = parameters["email"] ?: return@put call.respondText(
            text = "email Missing",
            status = HttpStatusCode.BadRequest
        )
        val password = parameters["password"] ?: return@put call.respondText(
            text = "password Missing",
            status = HttpStatusCode.BadRequest
        )
        try {
            val result = id.toLong().let { userId ->
                db.updateUsers(userId, username, email, password)
            }
            if (result == 1) {
                call.respondText(
                    text = "Update Successfully...",
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    "Something Went Wrong...",
                    status = HttpStatusCode.BadRequest
                )
            }

        } catch (e: Exception) {
            call.respondText(
                text = e.message.toString(),
                status = HttpStatusCode.BadRequest
            )
        }
    }
}

fun Route.category(
    db: CategoriesRepositoryImpl
) {
    post("v1/categories") {
        val parameters = call.receive<Parameters>()
        val name = parameters["name"] ?: return@post call.respondText(
            text = "Name is Missing",
            status = HttpStatusCode.BadRequest
        )
        val description = parameters["description"] ?: return@post call.respondText(
            text = "Description Missing",
            status = HttpStatusCode.BadRequest
        )
        val isVisible = parameters["isVisible"] ?: return@post call.respondText(
            text = "isVisible Missing",
            status = HttpStatusCode.BadRequest
        )
        val imageUrl = parameters["imageUrl"] ?: return@post call.respondText(
            text = "ImageUrl Missing",
            status = HttpStatusCode.BadRequest
        )

        try {
            val category = db.insertCategory(
                name, description, isVisible.toBoolean(), imageUrl
            )
            category?.id.let { categoryId ->
                call.respond(
                    status = HttpStatusCode.OK,
                    "Category Added - Success"
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                "Error Inserting the Category"
            )
        }
    }
    get("v1/categories") {
        try {
            val category = db.getAllCategories()
            if (category?.isNotEmpty() == true) {
                call.respond(
                    HttpStatusCode.OK,
                    "No Users Found"
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
    get("v1/categories/{id}") {
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
    delete("v1/categories/{id}") {
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
    put("v1/categories/{id}") {
        val id = call.parameters["id"]?.toLong() ?: return@put call.respondText(
            text = "Invalid Id",
            status = HttpStatusCode.BadRequest
        )
        val multipart = call.receiveMultipart()
        var name: String? = null
        var description: String? = null
        var isVisible: Boolean? = null
        var imageUrl: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "isVisible" -> isVisible = part.value.toBoolean()
                    }
                }

                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    val fileName =
                        part.originalFileName ?: "uploaded_image_${System.currentTimeMillis()}"
                    val directoryPath =
                        Paths.get("/var/www/uploads/") // Adjust the directory path as needed

                    // Create the directory if it doesn't exist
                    if (!Files.exists(directoryPath)) {
                        Files.createDirectories(directoryPath)
                    }

                    val filePath = "$directoryPath/$fileName"
                    Files.write(Paths.get(filePath), fileBytes)
                    imageUrl = "/uploads/categories/${fileName.replace(" ", "_")}"
                }

                is PartData.BinaryChannelItem -> TODO()
                is PartData.BinaryItem -> TODO()
            }
            part.dispose()
        }

        name ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            "Name Missing"
        )
        description ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            "Description Missing"
        )
        isVisible ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            "isVisible Missing"
        )
        imageUrl ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            "ImageUrl Missing"
        )

        try {
            val result = id.let { categoryId ->
                db.updateCategory(categoryId, name, description, isVisible, imageUrl)
            }
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
    post("v1/categories") {
        val threads = call.receiveMultipart()
        var name: String? = null
        var description: String? = null
        var isVisible: Boolean? = null
        var imageUrl: String? = null

        threads.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "name" -> name = part.value
                        "description" -> description = part.value
                        "isVisible" -> isVisible = part.value.toBoolean()
                    }
                }

                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    val fileName =
                        part.originalFileName ?: "uploaded_image_${System.currentTimeMillis()}"
                    val directoryPath = Paths.get("/var/www/uploads/")
                    if (!Files.exists(directoryPath)) {
                        Files.createDirectories(directoryPath)
                    }
                    val filePath = "$directoryPath/$fileName"
                    Files.write(Paths.get(filePath), fileBytes)
                    imageUrl = "/uploads/categories/${fileName.replace(" ", "_")}"
                }

                is PartData.BinaryChannelItem -> TODO()
                is PartData.BinaryItem -> TODO()
            }
            part.dispose()
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
        imageUrl ?: return@post call.respond(
            status = HttpStatusCode.BadRequest,
            "ImageUrl Missing"
        )

        try {
            val category = db.insertCategory(name!!, description!!, isVisible!!, imageUrl!!)
            category?.id.let { categoryId ->
                call.respond(
                    status = HttpStatusCode.OK,
                    "Category Successfully Added to Server : $category"
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                "Error Adding Category To Server : ${e.message}"
            )
        }
    }
}

fun Route.products(
    db: ProductsRepositoryImpl
) {
    post("v1/products") {
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
        var sold: Long? = null


        threads.forEachPart { segmentsData ->
            when (segmentsData) {
                is PartData.FileItem -> {
                    val fileName =
                        segmentsData.originalFileName ?: "image${System.currentTimeMillis()}"
                    val file = File("/upload/products", fileName)
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
                        "isAvailable" -> isAvailable = segmentsData.value.toBoolean()
                        "discount" -> discount = segmentsData.value.toLongOrNull()
                        "promotion" -> promotion = segmentsData.value
                        "productRating" -> productRating = segmentsData.value.toDoubleOrNull()
                        "sold" -> sold = segmentsData.value.toLongOrNull()
                    }
                }

                else -> {
                }
            }
        }
        try {
            val product = db.insertProducts(
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
                imageUrl ?: return@post call.respondText(
                    "Image URL Missing",
                    status = HttpStatusCode.BadRequest
                ),
                categoryName ?: return@post call.respondText(
                    "Category Name Missing",
                    status = HttpStatusCode.BadRequest
                ),
                categoryId ?: return@post call.respondText(
                    "Category ID Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                ),
                createdDate ?: return@post call.respondText(
                    "Created Date Missing",
                    status = HttpStatusCode.BadRequest
                ),
                updatedDate ?: return@post call.respondText(
                    "Updated Date Missing",
                    status = HttpStatusCode.BadRequest
                ),
                totalStock ?: return@post call.respondText(
                    "Total Stock Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                ),
                brand ?: return@post call.respondText(
                    "Brand Missing",
                    status = HttpStatusCode.BadRequest
                ),
                isAvailable ?: false,
                discount ?: return@post call.respondText(
                    "Discount Missing or Invalid",
                    status = HttpStatusCode.BadRequest
                ),
                promotion ?: return@post call.respondText(
                    "Promotion Missing",
                    status = HttpStatusCode.BadRequest
                ),
                productRating ?: 0.0,
                sold ?: return@post call.respondText(
                    "Missing Items Sold",
                    status = HttpStatusCode.BadRequest
                )
            )
            product?.id?.let {
                call.respond(
                    status = HttpStatusCode.Created,
                    "Product Created Successfully: $product"
                )
            }
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.InternalServerError,
                "Error While Creating Product: ${e.message}"
            )
        }
    }
    get("v1/products") {
        try {
            val items = db.getAllProducts()
        } catch (e: Exception) {

        }
    }
}
