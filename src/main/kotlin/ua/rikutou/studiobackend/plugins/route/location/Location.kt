package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.location(
    locationDataSource: LocationDataSource,
    studioDataSource: StudioDataSource,
    userDataSource: UserDataSource,
    galleryDataSource: GalleryDataSource
) {
    getLocationById(
        locationDataSource = locationDataSource,
        galleryDataSource = galleryDataSource,
    )
    createLocation(
        locationDataSource = locationDataSource,
        studioDataSource = studioDataSource,
        userDataSource = userDataSource
    )
    getLocations(
        locationDataSource = locationDataSource,
        galleryDataSource = galleryDataSource,
    )
}