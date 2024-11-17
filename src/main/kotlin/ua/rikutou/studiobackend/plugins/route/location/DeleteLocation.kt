package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.deleteLocation() {
    authenticate {
        delete("locations/{locationId}") {

            val locationDataSource by application.inject<LocationDataSource>()

            val locationId = call.parameters["locationId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "LocationId not found"
                    )
                )
                return@delete
            }
            locationDataSource.deleteById(locationId = locationId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Location deleted"
            )
        }
    }
}