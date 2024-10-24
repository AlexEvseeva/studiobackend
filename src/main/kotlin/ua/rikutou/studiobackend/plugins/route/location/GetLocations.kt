package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocations(
    locationDataSource: LocationDataSource
) {
    authenticate {
        get("locations") {
            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val locations = locationDataSource
                .getAllLocations(
                    studioId = studioId
                )
            if (locations.isEmpty()) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(HttpStatusCode.OK, message = locations)
        }
    }
}