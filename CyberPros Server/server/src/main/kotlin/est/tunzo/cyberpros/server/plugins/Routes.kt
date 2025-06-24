package est.tunzo.cyberpros.server.plugins

/**
 * Receives the body of the HTTP request as parameters.
 *
 * This function is typically used in Ktor routing to extract form or JSON parameters
 * from an incoming HTTP request.
 */
import est.tunzo.cyberpros.server.domain.repository.category.CategoriesRepositoryImpl
import est.tunzo.cyberpros.server.domain.repository.users.UsersRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import sun.jvm.hotspot.HelloWorld.e

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
                userRole)
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
            val parameters = call.receive<Parameters>()
            val name = parameters["name"] ?: return@put call.respondText(
                text = "Invalid Name",
                status = HttpStatusCode.BadRequest
            )
            val description = parameters["description"] ?: return@put call.respondText(
                text = "Description Invalid",
                status = HttpStatusCode.BadRequest
            )
            val isVisible = parameters["isVisible"] ?: return@put call.respondText(
                text = "isVisible Invalid",
                status = HttpStatusCode.BadRequest
            )
            val imageUrl = parameters["imageUrl"] ?: return@put call.respondText(
                text = "ImageUrl Invalid",
                status = HttpStatusCode.BadRequest
            )
            try {
                val result = id.let { categoryId ->
                    db.updateCategory(categoryId, name, description, isVisible.toBoolean(), imageUrl)
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
    }
}