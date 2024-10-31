package ua.rikutou.studiobackend.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.plugins.route.auth.auth
import ua.rikutou.studiobackend.plugins.route.location.location
import ua.rikutou.studiobackend.plugins.route.studio.studio
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.token.JwtTokenService
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService
import java.io.File

fun Application.configureRouting(
    galleryDataSource: GalleryDataSource,
    studioDataSource: StudioDataSource,
    userDataSource: UserDataSource,
    locationDataSource: LocationDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
    ) {
    routing {
        staticFiles(
            remotePath = "/uploads",
            dir = File("uploads")
        )

        auth(
            userDataSource = userDataSource,
            hashingService = hashingService,
            tokenService = tokenService,
            tokenConfig = tokenConfig
        )
        studio(
            userDataSource = userDataSource,
            studioDataSource = studioDataSource
        )
        location(
            locationDataSource = locationDataSource,
            studioDataSource = studioDataSource,
            userDataSource = userDataSource,
            galleryDataSource = galleryDataSource,
        )

    }
}
