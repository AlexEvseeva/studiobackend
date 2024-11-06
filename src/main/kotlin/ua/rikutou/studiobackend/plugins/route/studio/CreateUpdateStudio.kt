package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.studio.Studio
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.studio.requests.StudioRequest
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.createUpdateStudio() {
    authenticate {
        post("studio") {

            val studioDataSource by inject<StudioDataSource>()
            val userDataSource by inject<UserDataSource>()

            val request = call.runCatching {
                this.receiveNullable<StudioRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio fields not found."
                    )
                )
                return@post
            }

            if (request.studioId != null) {
                val studio = Studio (
                    studioId = request.studioId,
                    name = request.name,
                    address = request.address,
                    postIndex = request.postIndex,
                    site = request.site,
                    youtube = request.youtube,
                    facebook = request.facebook,
                )
                studioDataSource.updateStudio(studio)

                call.respond(HttpStatusCode.OK, message = studio)

            } else {
                val studioId = studioDataSource.insertStudio(
                    Studio(
                        name = request.name,
                        address = request.address,
                        postIndex = request.postIndex,
                        site = request.site,
                        youtube = request.youtube,
                        facebook = request.facebook,
                    )
                ) ?: run {
                    call.respond(
                        status = HttpStatusCode.Conflict,
                        message = Error(
                            code = HttpStatusCode.Conflict.value,
                            message = "Studio already exists."
                        )
                    )
                    return@post
                }
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toInt() ?: -1

                userDataSource.updateUser(userId = userId, studioId = studioId)
                call.respond(HttpStatusCode.OK,
                    Studio(
                        studioId = studioId,
                        name = request.name,
                        address = request.address,
                        postIndex = request.postIndex,
                        site = request.site,
                        youtube = request.youtube,
                        facebook = request.facebook,
                    )
                )
            }
        }
    }
}