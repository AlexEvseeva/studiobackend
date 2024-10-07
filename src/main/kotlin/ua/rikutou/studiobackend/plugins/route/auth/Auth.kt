package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService

fun Route.auth(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    signIn(
        userDataSource = userDataSource,
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig
    )

    signUp(
        hashingService = hashingService,
        userDataSource = userDataSource
    )

    getSecretInfo()
}