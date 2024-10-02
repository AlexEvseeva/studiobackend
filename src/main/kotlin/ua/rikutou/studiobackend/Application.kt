package ua.rikutou.studiobackend

import ua.rikutou.studiobackend.plugins.*
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ua.rikutou.studiobackend.data.user.PostgresUserDataSource
import ua.rikutou.studiobackend.data.user.User
import ua.rikutou.studiobackend.plugins.configureDataBases
import ua.rikutou.studiobackend.plugins.configureRouting
import ua.rikutou.studiobackend.plugins.configureSecurity
import ua.rikutou.studiobackend.plugins.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = environment.config
    val userDataSource = PostgresUserDataSource()

    configureDataBases(config = appConfig)
    configureSecurity()
    configureSerialization()
    configureRouting()

}
