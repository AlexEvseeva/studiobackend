package ua.rikutou.studiobackend.plugins.route.transport

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.transport.TransportDataSource
import kotlin.text.Typography.section

fun Route.getTransportById() {
    authenticate {
        get("/transportById") {
            val transportDataSource by application.inject<TransportDataSource>()

            val transportId = call.parameters["transportId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Transport Id not found"
                    )
                )
                return@get
            }

            val transport = transportDataSource.getTransportById(transportId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "Transport not found"
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