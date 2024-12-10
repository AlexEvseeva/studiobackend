package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.gallery.Gallery
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource

fun Route.getLocations() {
    authenticate {
        get("locations") {

            val locationDataSource by application.inject<LocationDataSource>()
            val galleryDataSource by application.inject<GalleryDataSource>()

            val studioId = call.runCatching { parameters["studioId"]?.toInt() }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found."
                    )
                )
                return@get
            }

            val search = call.parameters["search"]
            val type = call.parameters["type"]
            val widthFrom = call.runCatching { parameters["widthFrom"]?.toInt() }.getOrNull()
            val widthTo = call.runCatching { parameters["widthTo"]?.toInt() }.getOrNull()
            val lengthFrom = call.runCatching { parameters["lengthFrom"]?.toInt() }.getOrNull()
            val lengthTo = call.runCatching { parameters["lengthTo"]?.toInt() }.getOrNull()
            val heightFrom = call.runCatching { parameters["heightFrom"]?.toInt() }.getOrNull()
            val heightTo = call.runCatching { parameters["heightTo"]?.toInt() }.getOrNull()

            val locations = locationDataSource
                .getAllLocations(
                    studioId = studioId,
                    search = search,
                    type = type,
                    widthFrom = widthFrom,
                    widthTo = widthTo,
                    lengthFrom = lengthFrom,
                    lengthTo = lengthTo,
                    heightFrom = heightFrom,
                    heightTo = heightTo
                )
            if (locations.isEmpty()) {
                call.respond(status = HttpStatusCode.NotFound, message = Error(code = HttpStatusCode.NotFound.value, message = "Locations not found."))
                return@get
            }
            val locationToGallery: Map<Int, List<Gallery>> = locations.map {
                it.locationId
            }.filterNotNull().map { locationId ->
                locationId to galleryDataSource.getGalleryByLocationId(locationId)
            }.toMap()

            call.respond(
                HttpStatusCode.OK,
                message = locations.map { location ->
                    location.copy(
                        images = locationToGallery[location.locationId!!]
                    )
                }
            )
        }
    }
}