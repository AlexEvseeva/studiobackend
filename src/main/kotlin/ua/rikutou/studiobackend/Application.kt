package ua.rikutou.studiobackend

import ua.rikutou.studiobackend.plugins.*
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ua.rikutou.studiobackend.data.gallery.PostgresGalleryDataSource
import ua.rikutou.studiobackend.data.location.PostgresLocationDataSource
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.user.PostgresUserDataSource
import ua.rikutou.studiobackend.data.user.User
import ua.rikutou.studiobackend.di.appModule
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
    install(Koin) {
        slf4jLogger()
        modules(
            appModule(
                config = environment.config
            )
        )
    }
    
    configureSecurity()
    configureSerialization()
    configureRouting()

}
