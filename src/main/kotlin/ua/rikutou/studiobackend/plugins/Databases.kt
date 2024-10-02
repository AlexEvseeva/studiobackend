package ua.rikutou.studiobackend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDataBases(config: ApplicationConfig) {
    Database.connect(
        url = config.property("storage.jdbcURL").getString(),
        user = config.property("storage.user").getString(),
        password = System.getenv("PG_PASSWORD")
    )
}