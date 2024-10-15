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

fun Route.createStudio(
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

            val studioId = studioDataSource.insertStudio(
                Studio(
                    name = request.name,
                    address = request.address,
                    phone = request.phone,
                    email = request.email
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
                    phone = request.phone,
                    email = request.email)
            )
        }
    }
}