package ua.rikutou.studiobackend.plugins.route.actor

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.actor.Actor
import ua.rikutou.studiobackend.data.actor.ActorDataSource
import ua.rikutou.studiobackend.data.actor.requests.ActorRequest

fun Route.createUpdateActor() {
    authenticate {
        post("actor") {

            val actorDataSource by application.inject<ActorDataSource>()

            val request = call.runCatching {
                this.receiveNullable<ActorRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Actor's fields not found"
                    )
                )
                return@post
            }
            val actor = Actor(
                actorId = request.actorId,
                name = request.name,
                nickName = request.nickName,
                role = request.role,
                studioId = request.studioId,
            )
            val id = actorDataSource.insertUpdateActors(actor)
            call.respond(
                status = HttpStatusCode.OK,
                message = actor.copy(actorId = id)
            )
        }
    }
}