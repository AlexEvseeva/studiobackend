package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.requests.AuthRequest
import ua.rikutou.studiobackend.data.user.responses.AuthResponse
import ua.rikutou.studiobackend.security.hashing.HashingService
import ua.rikutou.studiobackend.security.hashing.SaltedHash
import ua.rikutou.studiobackend.security.token.TokenClaim
import ua.rikutou.studiobackend.security.token.TokenConfig
import ua.rikutou.studiobackend.security.token.TokenService

fun Route.signIn() {

    post("signin") {

        val userDataSource by inject<UserDataSource>()
        val hashingService by inject<HashingService>()
        val tokenConfig by inject<TokenConfig>()
        val tokenService by inject<TokenService>()

        val request = call.runCatching {
            this.receiveNullable<AuthRequest>()
        }.getOrNull() ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = Error(
                    code = HttpStatusCode.BadRequest.value,
                    message = "Name or password not found."
                )
            )
            return@post
        }

        val user = userDataSource.getUserByUserName(name = request.name) ?: run {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = Error(
                    code = HttpStatusCode.NotFound.value,
                    message = "User not found."
                )
            )
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
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = Error(
                    code = HttpStatusCode.BadRequest.value,
                    message = "Incorrect user name or password."
                )
            )
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = (user.userId ?: -1).toString()
            ),
            TokenClaim(
                name = "studioId",
                value = (user.studioId ?: -1).toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                userId = user.userId,
                userName = user.name,
                studioId = user.studioId
            )
        )
    }
}