package ua.rikutou.studiobackend.plugins.route.actor

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.actor.ActorDataSource

fun Route.getActorById() {
    authenticate {
        get("actorById") {
            val actorDataSource by application.inject<ActorDataSource>()

            val actorId = call.parameters["actorId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Actor id not found"
                    )
                )
                return@get
            }

            val actor = actorDataSource.getActorById(id = actorId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "Actor not found"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = actor
            )
        }
    }
}