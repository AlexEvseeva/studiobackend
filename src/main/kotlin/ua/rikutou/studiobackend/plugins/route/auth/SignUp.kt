package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.data.user.User
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.requests.AuthRequest
import ua.rikutou.studiobackend.security.hashing.HashingService

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.runCatching {
            this.receiveNullable<AuthRequest>()
        }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.name.isBlank() || request.password.isBlank()
        val isPasswordTooShort = request.password.length < 4

        if(areFieldsBlank || isPasswordTooShort) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)

        if(!userDataSource.insertUser(
            User(
                name = request.name,
                password = saltedHash.hash,
                salt = saltedHash.salt
            )
        )) {
            call.respond(HttpStatusCode.Conflict)
        } else {
            call.respond(HttpStatusCode.OK, "{}")
        }
    }
}