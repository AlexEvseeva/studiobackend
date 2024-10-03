package ua.rikutou.studiobackend.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.token.JwtTokenService
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
    ) {
    routing {

    }
}
