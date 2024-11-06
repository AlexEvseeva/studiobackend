package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.user.User
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.requests.AuthRequest
import ua.rikutou.studiobackend.security.hashing.HashingService

fun Route.signUp() {
    post("signup") {

        val hashingService by inject<HashingService>()
        val userDataSource by inject<UserDataSource>()

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

        val areFieldsBlank = request.name.isBlank() || request.password.isBlank()
        val isPasswordTooShort = request.password.length < 4

        if(areFieldsBlank || isPasswordTooShort) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = Error(
                    code = HttpStatusCode.BadRequest.value,
                    message = "Name or password must not be blank or password is too short"
                )
            )
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
            call.respond(
                status = HttpStatusCode.Conflict,
                message = Error(
                    code = HttpStatusCode.Conflict.value,
                    message = "User already exists"
                )
            )
        } else {
            call.respond(HttpStatusCode.OK, "{}")
        }
    }
}