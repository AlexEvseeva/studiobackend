package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocationById (
    locationDataSource: LocationDataSource
) {
    authenticate {
        get("locationById") {
            val locationId = call.parameters["locationId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Location id not found."
                    )
                )
                return@get
            }

            val location = locationDataSource.getLocationById(locationId = locationId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "Location not found."
                    )
                )
                return@get
            }

            call.respond(status = HttpStatusCode.OK, message = location)
        }
    }
}