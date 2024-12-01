package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource

fun Route.deleteStudio() {
    authenticate {
        delete( path ="studio/{studioId}") {
            val studioDataSource by application.inject<StudioDataSource>()

            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Studio id not found"
                )
                return@delete
            }

            studioDataSource.deleteStudioById(studioId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Studio successfully deleted"
            )
        }
    }
}