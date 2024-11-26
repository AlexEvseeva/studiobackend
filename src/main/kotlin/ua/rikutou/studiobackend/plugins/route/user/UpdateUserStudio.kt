package ua.rikutou.studiobackend.plugins.route.user

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.responses.StudioUser

fun Route.updateUserStudio() {
    authenticate {
        post("users") {
            val userDataSource by application.inject<UserDataSource>()

            var request = call.runCatching {
                this.receiveNullable<StudioUser>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "User information not found"
                    )
                )
                return@post
            }

            if (
                userDataSource.updateUser(
                    userId = request.userId,
                    studioId = request.studioId
                )
            ) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = StudioUser(
                        userId = request.userId,
                        userName = request.userName,
                        studioId = request.studioId
                    )
                )
            } else {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = Error(
                        code = HttpStatusCode.Conflict.value,
                        message = "User not updated"
                    )
                )
            }
        }
    }
}