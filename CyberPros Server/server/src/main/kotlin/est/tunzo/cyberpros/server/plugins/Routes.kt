package est.tunzo.cyberpros.server.plugins

import est.tunzo.cyberpros.server.data.local.table.users.UsersTable.username
import est.tunzo.cyberpros.server.domain.repository.users.UsersRepository
import io.ktor.client.utils.EmptyContent.status
import io.ktor.http.Parameters
/**
 * Receives the body of the HTTP request as parameters.
 *
 * This function is typically used in Ktor routing to extract form or JSON parameters
 * from an incoming HTTP request.
 */
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

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
            status = io.ktor.http.HttpStatusCode.Unauthorized
        )

        try {
            val users = db.insertUser(username, password, email)
                    users?.id.let {
                        call.respondText(
                            status = io.ktor.http.HttpStatusCode.OK,
                            text = "User Successfully Created"
                        )
                    }
        } catch (e: Exception) {
            call.respondText(
                status = io.ktor.http.HttpStatusCode.BadRequest,
                text = "Error Creating User"
            )
        }
    }
}