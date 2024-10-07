package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getSecretInfo() {
    authenticate {
        get("getinfo") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your user id: $userId")
        }
    }
}