package ua.rikutou.studiobackend.plugins.route.section

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.server.response.*
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.section.SectionDataSource

fun Route.getSectionById() {
    authenticate {
        get("sectionById") {

            val sectionDataSource by application.inject<SectionDataSource>()

            val sectionId = call.parameters["sectionId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "SectionId not found"
                    )
                )
                return@get
            }

            val section = sectionDataSource.getSectionById(sectionId) ?: kotlin.run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "Section not found"
                    )
                )
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = section
            )
        }
    }
}