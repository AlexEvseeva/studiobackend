package ua.rikutou.studiobackend.plugins.route.transport

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.transport.TransportDataSource
import ua.rikutou.studiobackend.data.transport.toTransportType

fun Route.getAllTransport() {
    authenticate {
        get("transport") {
            val transportDataSource by application.inject<TransportDataSource>()

            val studioId = call.runCatching { parameters["studioId"]?.toInt() }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio ID not found"
                    )
                )
                return@get
            }
            val search = call.parameters["search"]
            val type = call.runCatching { parameters["type"]?.toInt()?.toTransportType() }.getOrNull()
            val manufactureDateFrom = call.runCatching { parameters["manufactureDateFrom"]?.toLong() }.getOrNull()
            val manufactureDateTo = call.runCatching { parameters["manufactureDateFrom"]?.toLong() }.getOrNull()
            val seatsFrom = call.runCatching { parameters["seatsFrom"]?.toInt() }.getOrNull()
            val seatsTo = call.runCatching { parameters["seatsTo"]?.toInt() }.getOrNull()


            val transport = transportDataSource.getAllTransport(
                studioId = studioId,
                search = search,
                type = type,
                manufactureDateFrom = manufactureDateFrom,
                manufactureDateTo = manufactureDateTo,
                seatsFrom = seatsFrom,
                seatsTo = seatsTo
            )
            if (transport.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "No transport found"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = transport
            )
        }
    }
}