package ua.rikutou.studiobackend.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.plugins.route.auth.me
import ua.rikutou.studiobackend.plugins.route.auth.signIn
import ua.rikutou.studiobackend.plugins.route.auth.signUp
import ua.rikutou.studiobackend.plugins.route.location.createLocation
import ua.rikutou.studiobackend.plugins.route.location.getLocationById
import ua.rikutou.studiobackend.plugins.route.location.getLocations
import ua.rikutou.studiobackend.plugins.route.studio.createUpdateStudio
import ua.rikutou.studiobackend.plugins.route.studio.getStudio
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.token.JwtTokenService
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService
import java.io.File

fun Application.configureRouting() {
    routing {
        staticFiles(
            remotePath = "/uploads",
            dir = File("uploads")
        )
        signIn()
        signUp()
        me()

        getStudio()
        createUpdateStudio()

        getLocationById()
        createLocation()
        getLocations()

    }
}
