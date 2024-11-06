package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocationById () {
    authenticate {
        get("locationById") {

            val locationDataSource by inject<LocationDataSource>()
            val galleryDataSource by inject<GalleryDataSource>()

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
            val gallery = galleryDataSource.getGalleryByLocationId(locationId)

            call.respond(status = HttpStatusCode.OK, message = location.copy(images = gallery))
        }
    }
}