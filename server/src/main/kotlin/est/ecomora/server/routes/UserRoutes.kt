package est.ecomora.server.routes

import est.ecomora.server.domain.model.login.ErrorResponse
import est.ecomora.server.domain.model.login.LoginRequest
import est.ecomora.server.domain.model.login.LoginResponse
import est.ecomora.server.domain.model.login.RegisterRequest
import est.ecomora.server.domain.model.login.SuccessResponse
import est.ecomora.server.domain.repository.users.UsersRepositoryImpl
import est.ecomora.server.domain.service.JwtService
import est.ecomora.server.plugins.AppLogger
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRoutes(db: UsersRepositoryImpl) {
    
    route("/api/v1/auth") {
        post("/register") {
            try {
                // Try to receive as JSON first, fallback to form data
                val request = try {
                    call.receive<RegisterRequest>()
                } catch (e: Exception) {
                    // Fallback to form data parsing
                    try {
                        val parameters = call.receive<io.ktor.http.Parameters>()
                        RegisterRequest(
                            email = parameters["email"] ?: "",
                            password = parameters["password"] ?: "",
                            userName = parameters["userName"] ?: "",
                            fullName = parameters["fullName"],
                            phoneNumber = parameters["phoneNumber"],
                            userRole = parameters["userRole"] ?: "user",
                            imageUrl = parameters["imageUrl"]
                        )
                    } catch (formException: Exception) {
                        AppLogger.error("Failed to parse both JSON and form data", e)
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid request format. Expected JSON with email, password, userName fields")
                        )
                        return@post
                    }
                }

                // Basic validation
                if (request.email.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email is required"))
                    return@post
                }
                if (request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password is required"))
                    return@post
                }
                if (request.userName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Username is required"))
                    return@post
                }

                val user = db.insertUser(
                    request.userName,
                    request.password,
                    request.email,
                    request.phoneNumber ?: "",
                    request.userRole,
                    request.imageUrl ?: "",
                    request.fullName ?: ""
                )
                
                user?.let {
                    AppLogger.info("New user registered: {}", request.email)
                    call.respond(
                        HttpStatusCode.Created,
                        SuccessResponse("User registered successfully", user)
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to create user")
                    )
                }
            } catch (e: Exception) {
                AppLogger.error("Error during user registration", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error while creating user: ${e.message}")
                )
            }
        }

        post("/login") {
            try {
                // Try to receive as JSON first, fallback to form data
                val request = try {
                    call.receive<LoginRequest>()
                } catch (e: Exception) {
                    // Fallback to form data parsing
                    try {
                        val parameters = call.receive<io.ktor.http.Parameters>()
                        LoginRequest(
                            email = parameters["email"] ?: "",
                            password = parameters["password"] ?: ""
                        )
                    } catch (formException: Exception) {
                        AppLogger.error("Failed to parse both JSON and form data", e)
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid request format. Expected JSON: {\"email\":\"...\", \"password\":\"...\"}")
                        )
                        return@post
                    }
                }

                if (request.email.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email is required"))
                    return@post
                }
                if (request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password is required"))
                    return@post
                }

                val user = db.login(request.email, request.password)
                
                if (user != null) {
                    val token = JwtService.generateToken(user.id.toString(), user.userRole)
                    AppLogger.info("Successful login for user: {}", request.email)
                    
                    val loginResponse = LoginResponse(
                        message = "Login successful",
                        user = user,
                        token = token
                    )
                    call.respond(HttpStatusCode.OK, loginResponse)
                } else {
                    AppLogger.warn("Failed login attempt for user: {}", request.email)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid email or password")
                    )
                }
            } catch (e: Exception) {
                AppLogger.error("Error during login", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error during login: ${e.message}")
                )
            }
        }
    }

    route("/api/v1/users") {
        get {
            try {
                val users = db.getAllUsers()

                if (users?.isNotEmpty() == true) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Users fetched successfully", users)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("No users found")
                    )
                }
            } catch (e: Exception) {
                AppLogger.error("Error fetching users", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error fetching users: ${e.message}")
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid user ID")
                )
                return@get
            }

            try {
                val user = db.getUsersById(id)
                if (user != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("User fetched successfully", user)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error fetching user: ${e.message}")
                )
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid user ID")
                )
                return@delete
            }

            try {
                val deletedCount = db.deleteUserById(id)
                if (deletedCount == 1) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("User deleted successfully", null)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error deleting user: ${e.message}")
                )
            }
        }
    }
}