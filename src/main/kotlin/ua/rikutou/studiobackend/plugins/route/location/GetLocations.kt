package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocations(
    locationDataSource: LocationDataSource
) {
    authenticate {
        get("locations") {
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

            val locations = locationDataSource
                .getAllLocations(
                    studioId = studioId
                )
            if (locations.isEmpty()) {
                call.respond(status = HttpStatusCode.NotFound, message = Error(code = HttpStatusCode.NotFound.value, message = "Locations not found."))
                return@get
            }
            call.respond(HttpStatusCode.OK, message = locations)
        }
    }
}