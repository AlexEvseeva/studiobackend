package ua.rikutou.studiobackend.plugins.route.location

import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.location(
    locationDataSource: LocationDataSource,
    studioDataSource: StudioDataSource,
    userDataSource: UserDataSource
) {
    getLocation(
        locationDataSource = locationDataSource
    )
    createLocation(
        locationDataSource = locationDataSource,
        studioDataSource = studioDataSource,
        userDataSource = userDataSource
    )
}