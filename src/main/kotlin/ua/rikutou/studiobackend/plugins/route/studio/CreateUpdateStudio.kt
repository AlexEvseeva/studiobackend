package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.studio.Studio
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.studio.requests.StudioRequest
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.createUpdateStudio(
    studioDataSource: StudioDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("studio") {
            val request = call.runCatching {
                this.receiveNullable<StudioRequest>()
            }.getOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest)
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
                    call.respond(HttpStatusCode.Conflict)
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