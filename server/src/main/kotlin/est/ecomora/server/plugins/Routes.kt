package est.ecomora.server.plugins


/**
 * Provides utility for working with file system paths in Kotlin.
 *
 * This import allows for creating, manipulating, and resolving file system paths
 * using the java.nio.file.Paths utility class.
 */
import est.ecomora.server.domain.model.login.LoginResponse
import est.ecomora.server.domain.repository.category.CategoriesRepositoryImpl
import est.ecomora.server.domain.repository.products.ProductsRepositoryImpl
import est.ecomora.server.domain.repository.promotions.PromotionsRepositoryImpl
import est.ecomora.server.domain.repository.services.EservicesRepositoryImpl
import est.ecomora.server.domain.repository.users.UsersRepositoryImpl
import io.ktor.http.ContentType
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
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat

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
    db: UsersRepositoryImpl
) {

    post("v1/users") {
        val parameters = call.receive<Parameters>()
        val username = parameters["username"] ?: return@post call.respondText(
            text = " Username is Missing, Please provide a username",
            status = HttpStatusCode.Unauthorized
        )

        val email = parameters["email"] ?: return@post call.respondText(
            text = " Email is Missing, Please provide an email",
            status = HttpStatusCode.Unauthorized
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
                username, password, email, phoneNumber, userRole, "", fullName
            )
            users?.id.let {
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
    post("v1/login") {
        val parameters = call.receive<Parameters>()
        val email = parameters["email"] ?: return@post call.respondText(
            text = "Email Missing",
            status = HttpStatusCode.Unauthorized
        )
        val password = parameters["password"] ?: return@post call.respondText(
            text = "Password Missing",
            status = HttpStatusCode.Unauthorized
        )

        try {
            val user = db.login(email, password)
            if (user != null) {
                val loginResponse = LoginResponse("Login Successfully", user)
                val userJsonData = Json{prettyPrint = true}.encodeToString(loginResponse)
                call.respondText(userJsonData, ContentType.Application.Json)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid Email or Password")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error during login: ${e.message}")
        }
    }
    get("v1/users") {
        try {
            val users = db.getAllUsers()
            if (users?.isNotEmpty() == true) {
                call.respond(HttpStatusCode.OK, users)
            }
        } catch(e: Exception) {
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
            if(users == null) {
                return@get call.respondText(
                    text = "User Not Found",
                    status = HttpStatusCode.NotFound
                )
            }
            else {
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
            }?: return@delete call.respondText(
                "No Id Found",
                status = HttpStatusCode.BadRequest
            )

            if (users == 1) {
                call.respondText(
                    "Deleted User Successfully",
                    status = HttpStatusCode.OK
                )
            }
            else {
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

        val fullName = parameters["fullName"] ?: return@put call.respondText(
            text = "fullName Missing",
            status = HttpStatusCode.BadRequest
        )

        val phoneNumber = parameters["phoneNumber"] ?: return@put call.respondText(
            text = "fullName Missing",
            status = HttpStatusCode.BadRequest
        )

        try {
            val result = id.toLong().let { userId ->
                db.updateUsers(
                    userId, username, email, password, fullName ,  phoneNumber
                )
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
        val multipart = call.receiveMultipart()
        var name: String? = null
        var description: String? = null
        var isVisible: Boolean? = null
        var imageUrl: String? = null
        val uploadDir = File("upload/products/categories")
        if (!uploadDir.exists()){
            uploadDir.mkdirs()
        }

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
                    val fileName = part.originalFileName?.replace(" ", "_") ?: "image${System.currentTimeMillis()}"
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
        imageUrl ?: return@post call.respond(
            status = HttpStatusCode.BadRequest,
            "ImageUrl Missing"
        )

        try {
            val category = db.insertCategory(name!!, description!!, isVisible!!, imageUrl!!)
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

    get("v1/categories") {
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
        val uploadDir = File("upload/products/categories")
        if (!uploadDir.exists()){
            uploadDir.mkdirs()
        }

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
                    val fileName = part.originalFileName?.replace(" ", "_") ?: "image${System.currentTimeMillis()}"
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
            val result = db.updateCategory(id, name!!, description!!, isVisible!!, imageUrl!!)
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
        val uploadDir = File("upload/products/categories")
        if(uploadDir.exists()) {
            uploadDir.mkdirs()
        }

        threads.forEachPart { part->
            when(part) {
                is PartData.FormItem -> {
                    when(part.name) {
                        "name" -> name = part.value
                        "description" ->description = part.value
                        "isVisible" -> isVisible = part.value.toBoolean()
                    }
                }

                is PartData.FileItem -> {
                    val fileName = part.originalFileName?.replace(" ", "_") ?: "image${System.currentTimeMillis()}"
                    val file = File(uploadDir, fileName)
                    part.streamProvider().use { input ->
                        file.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }

                    }
                    imageUrl = "/upload/products/categories/${fileName}"
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
        val uploadsDirectory = File("uploads/products/")
        if (uploadsDirectory.exists()) {
            uploadsDirectory.mkdirs()
        }


        threads.forEachPart { segmentsData ->
            when(segmentsData) {
                is PartData.FileItem -> {
                    val fileName = segmentsData.originalFileName ?.replace("","")?: "Image${System.currentTimeMillis()}"
                    val file = File(uploadsDirectory, fileName)
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
                        "isAvailable" -> isAvailable = segmentsData.value.toBoolean()
                        "discount" -> discount = segmentsData.value.toLongOrNull()
                        "promotion" -> promotion = segmentsData.value
                        "productRating" -> productRating = segmentsData.value.toDoubleOrNull()
                    }
                }
                else -> {
                }
            }
        }
        try {
            val product = db.insertProducts(
                name ?: return@post call.respondText("Name Missing", status = HttpStatusCode.BadRequest),
                description ?: return@post call.respondText("Description Missing", status = HttpStatusCode.BadRequest),
                price ?: return@post call.respondText("Price Missing or Invalid", status = HttpStatusCode.BadRequest),
                imageUrl ?: return@post call.respondText("Image URL Missing", status = HttpStatusCode.BadRequest),
                categoryName ?: return@post call.respondText("Category Name Missing", status = HttpStatusCode.BadRequest),
                categoryId ?: return@post call.respondText("Category ID Missing or Invalid", status = HttpStatusCode.BadRequest),
                createdDate ?: return@post call.respondText("Created Date Missing", status = HttpStatusCode.BadRequest),
                updatedDate ?: return@post call.respondText("Updated Date Missing", status = HttpStatusCode.BadRequest),
                totalStock ?: return@post call.respondText("Total Stock Missing or Invalid", status = HttpStatusCode.BadRequest),
                brand ?: return@post call.respondText("Brand Missing", status = HttpStatusCode.BadRequest),
                isAvailable ?: false,
                discount ?: return@post call.respondText("Discount Missing or Invalid", status = HttpStatusCode.BadRequest),
                promotion ?: "",
                productRating ?: 0.0
            )
            product?.id?.let {
                call.respond(
                    status = HttpStatusCode.Created,
                    "Product Created Successfully: $product"
                )
            }
        }catch (e: Exception){
            call.respond(
                status = HttpStatusCode.InternalServerError,
                "Error While Creating Product: ${e.message}"
            )
        }
    }
    get("v1/products") {
        try {
            val items = db.getAllProduct()
            if (items?.isNotEmpty()==true){
                call.respond(HttpStatusCode.OK,
                    items)
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
    get("v1/products/{id}") {
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

    delete("v1/products/{id}") {
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
    put("v1/products/{id}") {
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

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FileItem -> {
                    val fileName = partData.originalFileName ?.replace("","")?: "Image${System.currentTimeMillis()}"
                    val file = File("/upload/products", fileName)
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
                        "isAvailable" -> isAvailable = partData.value.toBoolean()
                        "discount" -> discount = partData.value.toLongOrNull()
                        "promotion" -> promotion = partData.value
                        "productRating" -> productRating = partData.value.toDoubleOrNull()
                    }
                }
                else -> {
                }
            }
            partData.dispose()
        }

        try {
            val result = db.updateProductById(
                id,
                name ?: return@put call.respondText("Name Missing", status = HttpStatusCode.BadRequest),
                description ?: return@put call.respondText("Description Missing", status = HttpStatusCode.BadRequest),
                price ?: return@put call.respondText("Price Missing or Invalid", status = HttpStatusCode.BadRequest),
                imageUrl ?: return@put call.respondText("Image URL Missing", status = HttpStatusCode.BadRequest),
                categoryName ?: return@put call.respondText("Category Name Missing", status = HttpStatusCode.BadRequest),
                categoryId ?: return@put call.respondText("Category ID Missing or Invalid", status = HttpStatusCode.BadRequest),
                createdDate ?: return@put call.respondText("Created Date Missing", status = HttpStatusCode.BadRequest),
                updatedDate ?: return@put call.respondText("Updated Date Missing", status = HttpStatusCode.BadRequest),
                totalStock ?: return@put call.respondText("Total Stock Missing or Invalid", status = HttpStatusCode.BadRequest),
                brand ?: return@put call.respondText("Brand Missing", status = HttpStatusCode.BadRequest),
                isAvailable ?: false,
                discount ?: return@put call.respondText("Discount Missing or Invalid", status = HttpStatusCode.BadRequest),
                promotion ?: "",
                productRating ?: 0.0
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

fun Route.services(
    db: EservicesRepositoryImpl
) {
    post("v1/services") {
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
        val uploadsDirectory = File("uploads/services/")
        if (uploadsDirectory.exists()) {
            uploadsDirectory.mkdirs()
        }

        threads.forEachPart { segmentsData ->
            when(segmentsData) {
                is PartData.FileItem -> {
                    val fileName = segmentsData.originalFileName ?.replace("","")?: "Image${System.currentTimeMillis()}"
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
                        "isVisible" -> isVisible = segmentsData.value.toBoolean()
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
                imageUrl ?: return@post call.respondText(
                    "Image URL Missing",
                    status = HttpStatusCode.BadRequest
                ),
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
    get("v1/services") {
        try {
            val items = db.getAllServices()
            if (items?.isNotEmpty()==true){
                call.respond(HttpStatusCode.OK,
                    items)
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
    get("v1/services/{id}") {
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
    delete("v1/services/{id}") {
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
    put("v1/services/{id}") {
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
                    val fileName = partData.originalFileName ?.replace("","")?: "Image${System.currentTimeMillis()}"
                    val file = File("/upload/services", fileName)
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
                        "isVisible" -> isVisible = partData.value.toBoolean()
                    }
                }
                else -> {
                }
            }
            partData.dispose()
        }

        try {
            val result = db.updateService(
                id,
                name ?: return@put call.respondText("Name Missing", status = HttpStatusCode.BadRequest),
                description ?: return@put call.respondText("Description Missing", status = HttpStatusCode.BadRequest),
                price ?: return@put call.respondText("Price Missing or Invalid", status = HttpStatusCode.BadRequest),
                offered ?: 0,
                category ?: return@put call.respondText(
                    "Category Missing",
                    status = HttpStatusCode.BadRequest
                ),
                imageUrl ?: return@put call.respondText("Image URL Missing", status = HttpStatusCode.BadRequest),
                isVisible ?: false,
                createdAt ?: return@put call.respondText(
                    "Created Date Missing",
                    status = HttpStatusCode.BadRequest
                ),
                updatedDate ?: return@put call.respondText(
                    "Updated Date Missing",
                    status = HttpStatusCode.BadRequest
                )
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
                status = HttpStatusCode.BadRequest,
                "Error While Updating Service: ${e.message}"
            )
        }
    }
}
fun Route.promotions(
    db: PromotionsRepositoryImpl
) {
    post("v1/promotions") {
        val multipart = call.receiveMultipart()
        var title: String? = null
        var description: String? = null
        var imageUrl: String? = null
        var startDate: Long? = null
        var endDate: Long? = null
        var enable: Boolean? = null
        val uploadDir = File("upload/products/promotions")
        if (!uploadDir.exists()){
            uploadDir.mkdirs()
        }
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FileItem -> {
                    val fileName = partData.originalFileName?.replace(" ","_") ?: "name/${System.currentTimeMillis()}"
                    val file = File(uploadDir,fileName)
                    partData.streamProvider().use { input->
                        file.outputStream().use { output->
                            input.copyTo(output)
                        }
                    }
                    imageUrl = "/upload/products/promotions/$fileName"
                }

                is PartData.FormItem -> {
                    when(partData.name){
                        "title" -> title = partData.value
                        "description" -> description = partData.value
                        "startDate" ->startDate= partData.value?.let { dateFormat.parse(it)?.time }
                        "endDate" -> endDate = partData.value?.let { dateFormat.parse(it)?.time }
                        "enable" -> enable = partData.value.toBoolean()
                    }
                }
                else -> {}
            }
        }
        try {
            val products = db.insertPromo(
                title  = title ?: return@post call.respond(HttpStatusCode.BadRequest, "Title Missing"),
                description = description ?: return@post call.respond(HttpStatusCode.BadRequest, "Description Missing"),
                imageUrl = imageUrl ?: return@post call.respond(HttpStatusCode.BadRequest, "Image File Missing"),
                startDate = startDate ?: return@post call.respond(HttpStatusCode.BadRequest, "Start Date Missing"),
                endDate = endDate ?: return@post call.respond(HttpStatusCode.BadRequest,"End Date Missing"),
                enabled = enable ?: return@post call.respond(HttpStatusCode.BadRequest,"Enabled Missing")
            )
            products.let {
                call.respond(
                    HttpStatusCode.Created,
                    "Promotion Product Added Successfully... $products"
                )
            }

        }catch (e: Exception){
            call.respond(
                status = HttpStatusCode.Unauthorized,
                "Error While Uploading Promotions Products : ${e.message}"
            )
        }
    }
    delete("v1/promotions/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

        try {
            val deletedCount = db.deletePromotionById(id)
            if (deletedCount != null && deletedCount > 0) {
                call.respond(HttpStatusCode.OK, "Promotion with ID $id deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Promotion with ID $id not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to delete promotion: ${e.message}")
        }
    }
    get("v1/promotions/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

        try {
            val promotion = db.getPromotionById(id)
            if (promotion != null) {
                call.respond(HttpStatusCode.OK, promotion)
            } else {
                call.respond(HttpStatusCode.NotFound, "Promotion with ID $id not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve promotion: ${e.message}")
        }
    }
    put ("v1/promotions/{id}"){
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            "Id Missing"
        )
        val multipart = call.receiveMultipart()
        var title : String? = null
        var description: String? = null
        var imageUrl: String? = null
        var startDate: Long? = null
        var endDate: Long? = null
        var enable: Boolean? = null
        val uploadDir = File("upload/products/promotions")
        if (!uploadDir.exists()){
            uploadDir.mkdirs()
        }
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FileItem -> {
                    val fileName = partData.originalFileName?.replace(" ","_") ?: "name/${System.currentTimeMillis()}"
                    val file = File(uploadDir,fileName)
                    partData.streamProvider().use { input->
                        file.outputStream().use { output->
                            input.copyTo(output)
                        }
                    }
                    imageUrl = "/upload/products/promotions/$fileName"
                }

                is PartData.FormItem -> {
                    when(partData.name){
                        "title" -> title = partData.value
                        "description" -> description = partData.value
                        "startDate" ->startDate= partData.value?.let { dateFormat.parse(it)?.time }
                        "endDate" -> endDate = partData.value?.let { dateFormat.parse(it)?.time }
                        "enable" -> enable = partData.value.toBoolean()
                    }
                }
                else ->{}
            }
        }
        try {
            val services = db.updatePromo(
                id = id.toLong(),
                title  = title ?: return@put call.respond(HttpStatusCode.BadRequest, "Title Missing"),
                description = description ?: return@put call.respond(HttpStatusCode.BadRequest, "Description Missing"),
                imageUrl = imageUrl ?: return@put call.respond(HttpStatusCode.BadRequest, "Image File Missing"),
                startDate = startDate ?: return@put call.respond(HttpStatusCode.BadRequest, "Start Date Missing"),
                endDate = endDate ?: return@put call.respond(HttpStatusCode.BadRequest,"End Date Missing"),
                enabled = enable ?: return@put call.respond(HttpStatusCode.BadRequest,"Enabled Missing")
            )
            if (services != null) {
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

        }catch (e: Exception){
            call.respond(
                status = HttpStatusCode.Unauthorized,
                "Error While Uploading Promotions Products : ${e.message}"
            )
        }
    }
    get("v1/promotions") {
        try {
            val promotions = db.getAllPromotions()
            if (promotions.isNullOrEmpty() == true ) {
                call.respond(
                    HttpStatusCode.NotFound,
                    "No Promotion Items Available inside the Database."
                )
            } else {
                call.respond(HttpStatusCode.OK, promotions)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve promotion: ${e.message}")
        }
    }
}
