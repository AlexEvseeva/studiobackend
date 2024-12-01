package ua.rikutou.studiobackend.plugins.route.transport

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.transport.TransportDataSource

fun Route.getAllTransport() {
    authenticate {
        get("transport") {
            val transportDataSource by application.inject<TransportDataSource>()

            val departmentId = call.parameters["departmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Department ID not found"
                    )
                )
                return@get
            }

            val search = call.parameters["search"]

            val transport = transportDataSource.getAllTransport(departmentId = departmentId, search = search)
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