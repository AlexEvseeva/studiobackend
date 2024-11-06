package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.location.Location
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.location.requests.LocationRequest
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.createLocation() {
    authenticate {
        post("location") {

            val locationDataSource by inject<LocationDataSource>()

            val request = call.runCatching {
                this.receiveNullable<LocationRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Location's fields not found"
                    )
                )
                return@post
            }

            val location = Location(
                name = request.name,
                address = request.address,
                width = request.width,
                length = request.length,
                height = request.height,
                type = request.type,
                studioId = request.studioId,
                rentPrice = request.rentPrice,
            )
            val locationId = locationDataSource.insertLocation(location)

            call.respond(HttpStatusCode.OK, location.copy(locationId = locationId))
        }
    }
}