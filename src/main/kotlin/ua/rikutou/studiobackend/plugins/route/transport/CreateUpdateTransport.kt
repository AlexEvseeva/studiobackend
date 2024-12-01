package ua.rikutou.studiobackend.plugins.route.transport

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.transport.Transport
import ua.rikutou.studiobackend.data.transport.TransportDataSource
import ua.rikutou.studiobackend.data.transport.requests.TransportRequest

fun Route.createUpdateTransport() {
    authenticate {
        post("transport") {

            val transportDataSource by application.inject<TransportDataSource>()

            val request = call.runCatching {
                this.receiveNullable<TransportRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Transport's fields not found"
                    )
                )
                return@post
            }

            val transport = Transport(
                transportId = request.transportId,
                type = request.type,
                mark = request.mark,
                manufactureDate = request.manufactureDate,
                seats = request.seats,
                departmentId = request.departmentId,
                color = request.color,
                technicalState = request.technicalState
            )
            val id = transportDataSource.insertUpdateTransport(transport = transport)
            call.respond(
                status = HttpStatusCode.OK,
                message = transport.copy(transportId = id)
            )
        }
    }

}