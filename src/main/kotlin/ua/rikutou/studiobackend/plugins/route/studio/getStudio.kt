package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.studio.StudioDataSource

fun Route.getStudio() {
    authenticate {
        get("studio") {
            val studioDataSource by inject<StudioDataSource>()
            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found."
                    )
                )
                return@get
            }

            val studio = studioDataSource.getStudioById(studioId = studioId) ?: run {
                call.respond(status = HttpStatusCode.NotFound, message = Error(code = HttpStatusCode.NotFound.value, message = "Studio not found."))
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = studio)
        }
    }
}