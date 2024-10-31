package ua.rikutou.studiobackend

import ua.rikutou.studiobackend.plugins.*
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ua.rikutou.studiobackend.data.gallery.PostgresGalleryDataSource
import ua.rikutou.studiobackend.data.location.PostgresLocationDataSource
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.user.PostgresUserDataSource
import ua.rikutou.studiobackend.data.user.User
import ua.rikutou.studiobackend.plugins.configureRouting
import ua.rikutou.studiobackend.plugins.configureSecurity
import ua.rikutou.studiobackend.plugins.configureSerialization
import ua.rikutou.studiobackend.security.hashing.SHA256HashingService
import ua.rikutou.studiobackend.security.token.JwtTokenService
import ua.rikutou.studiobackend.security.token.TokenConfig
import java.sql.DriverManager

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = environment.config
    val connection = DriverManager.getConnection(
        appConfig.property("storage.jdbcURL").getString(),
        appConfig.property("storage.user").getString(),
        System.getenv("PG_PASSWORD")
    )

    val userDataSource = PostgresUserDataSource(connection)
    val studioDataSource = PostgresStudioDataSource(connection)
    val locationDataSource = PostgresLocationDataSource(connection)
    val galleryDataSource = PostgresGalleryDataSource(connection)

    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val tokenService = JwtTokenService()
    val hashingService = SHA256HashingService()

    configureSecurity(config = tokenConfig)
    configureSerialization()
    configureRouting(
        galleryDataSource = galleryDataSource,
        userDataSource = userDataSource,
        studioDataSource = studioDataSource,
        locationDataSource = locationDataSource,
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig
    )

}
