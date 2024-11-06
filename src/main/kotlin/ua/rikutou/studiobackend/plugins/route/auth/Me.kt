package ua.rikutou.studiobackend.plugins.route.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.user.UserDataSource
import ua.rikutou.studiobackend.data.user.responses.MeResponse

fun Route.me() {
    authenticate {
        get("me") {

            val userDataSource by inject<UserDataSource>()

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt() ?: -1
            val user = userDataSource.getUserById(userId)
            call.respond(
                status = HttpStatusCode.OK,
                message =  MeResponse(
                    userId = user?.userId,
                    userName = user?.name,
                    studioId = user?.studioId
                )
            )
        }
    }
}