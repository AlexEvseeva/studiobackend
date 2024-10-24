package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocationById (
    locationDataSource: LocationDataSource
) {
    authenticate {
        get("locationById") {
            val locationId = call.parameters["locationId"]?.toInt() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val location = locationDataSource.getLocationById(locationId = locationId) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = location)
        }
    }
}