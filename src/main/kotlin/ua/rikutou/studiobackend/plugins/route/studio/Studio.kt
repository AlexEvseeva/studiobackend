package ua.rikutou.studiobackend.plugins.route.studio

import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource

fun Route.studio(
    userDataSource: UserDataSource,
    studioDataSource: StudioDataSource
) {
    getStudio(
        studioDataSource = studioDataSource
    )

    createStudio(
        studioDataSource = studioDataSource,
        userDataSource = userDataSource
    )
}