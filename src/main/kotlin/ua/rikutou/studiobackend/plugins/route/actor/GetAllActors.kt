package ua.rikutou.studiobackend.plugins.route.actor

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.actor.ActorDataSource

fun Route.getAllActors() {
    authenticate {
        get("actor") {
            val actorDataSource by application.inject<ActorDataSource>()
            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found."
                    )
                )
                return@get
            }
            val search = call.parameters["search"]

            val actors = actorDataSource.getAllActors(studioId = studioId, search = search)
            if (actors.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "No actors found."
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = actors
            )
        }
    }
}