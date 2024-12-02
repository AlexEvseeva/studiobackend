package ua.rikutou.studiobackend.plugins.route.actor

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.actor.ActorDataSource

fun Route.deleteActor() {
    authenticate {
        delete("actor/{actorId}") {
            val actorDataSource by application.inject<ActorDataSource>()

            val actorId = call.parameters["actorId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Actor Id not found"
                    )
                )
                return@delete
            }

            actorDataSource.deleteById(actorId = actorId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Actor deleted"
            )
        }
    }
}