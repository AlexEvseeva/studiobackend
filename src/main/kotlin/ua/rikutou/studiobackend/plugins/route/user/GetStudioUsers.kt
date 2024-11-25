package ua.rikutou.studiobackend.plugins.route.user

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Routing.getUsersByStudioIdAndCandidates() {
    authenticate {
        get("users") {

            val userDataSource by application.inject<UserDataSource>()

            val studioId = call.parameters["studioId"]?.toIntOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found."
                    )
                )
                return@get
            }

            val users = userDataSource.getStudioUsersAndCandidates(studioId = studioId)
            if(users.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "User not found"
                    )
                )
            }

            call.respond(status = HttpStatusCode.OK, message = users)
        }
    }
}