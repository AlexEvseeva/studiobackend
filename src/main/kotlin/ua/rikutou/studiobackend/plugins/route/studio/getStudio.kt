package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.studio.StudioDataSource

fun Route.getStudio(
    studioDataSource: StudioDataSource
) {
    authenticate {
        get("studio") {
            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val studio = studioDataSource.getStudioById(studioId = studioId) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = studio)
        }
    }
}