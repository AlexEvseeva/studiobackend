package ua.rikutou.studiobackend.plugins.route.user

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.deleteUser() {
    authenticate {
        delete ( "users/{userId}") {

            val userDataSource by application.inject<UserDataSource>()

            val userId = call.parameters["userId"]?.toIntOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "User id not found"
                    )
                )
                return@delete
            }

            userDataSource.deleteUserById(userId)
            call.respond(
                status = HttpStatusCode.OK,
                message = "User deleted"
            )
        }
    }
}
