package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.requests.AuthRequest
import ua.rikutou.studiobackend.data.user.responses.AuthResponse
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.hashing.SaltedHash
import ua.rikutou.studiobackend.security.token.TokenClaim
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService

fun Route.signIn(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.runCatching {
            this.receiveNullable<AuthRequest>()
        }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUserName(name = request.name) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        if(
            !hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.password,
                    salt = user.salt
                )
            )
        ) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = (user.userId ?: 0).toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}