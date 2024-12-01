package ua.rikutou.studiobackend.plugins.route.transport

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.transport.TransportDataSource

fun Route.deleteTransport() {
    authenticate {
        delete("transport/{transportId}") {
            val transportDataSource by application.inject<TransportDataSource>()

            val transportId = call.parameters["transportId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Transport Id not found"
                    )
                )
                return@delete
            }

            transportDataSource.deleteTransport(transportId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Transport deleted")
        }
    }
}