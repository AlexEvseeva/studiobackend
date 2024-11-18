package ua.rikutou.studiobackend.di

import io.ktor.server.config.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ua.rikutou.studiobackend.data.department.DepartmentDataSource
import ua.rikutou.studiobackend.data.department.PostgresDepartmentDataSource
import ua.rikutou.studiobackend.data.user.PostgresUserDataSource
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.studio.StudioDataSource
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.location.LocationDataSource
import ua.rikutou.studiobackend.data.location.PostgresLocationDataSource
import ua.rikutou.studiobackend.data.gallery.GalleryDataSource
import ua.rikutou.studiobackend.data.gallery.PostgresGalleryDataSource
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService
import ua.rikutou.studiobackend.security.token.JwtTokenService
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.hashing.SHA256HashingService
import java.sql.Connection
import java.sql.DriverManager

fun appModule(
    config: ApplicationConfig,
) = module {
    single { config }
    single<Connection> {
        DriverManager.getConnection(
            config.property("storage.jdbcURL").getString(),
            config.property("storage.user").getString(),
            System.getenv("PG_PASSWORD")
        )
    }
    singleOf(::PostgresUserDataSource) { bind<UserDataSource>()}
    singleOf(::PostgresStudioDataSource) { bind<StudioDataSource>()}
    singleOf(::PostgresLocationDataSource) { bind<LocationDataSource>()}
    singleOf(::PostgresGalleryDataSource) { bind<GalleryDataSource>() }
    singleOf(::PostgresDepartmentDataSource) { bind<DepartmentDataSource>()}

    single<TokenConfig> {
        TokenConfig(
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            expiresIn = 365L * 1000L * 60L * 60L * 24L,
            secret = System.getenv("JWT_SECRET")
        )
    }

    singleOf(::JwtTokenService){ bind<TokenService>() }
    singleOf(::SHA256HashingService) { bind<HashingService>()}
}