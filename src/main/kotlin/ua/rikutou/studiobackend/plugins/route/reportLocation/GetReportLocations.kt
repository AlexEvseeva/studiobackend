package ua.rikutou.studiobackend.plugins.route.reportLocation

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.reportLocation.ReportLocation
import ua.rikutou.studiobackend.data.reportLocation.ReportLocationDataSource

fun Route.getReportLocation() {
    authenticate {
        get("/reportLocation") {
            val reportLocationDataSource by application.inject<ReportLocationDataSource>()

            val studioId: Int = call.runCatching { parameters["studioId"]?.toInt() }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found"
                    )
                )
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = reportLocationDataSource.getReportLocation(studioId = studioId)
            )
        }
    }
}