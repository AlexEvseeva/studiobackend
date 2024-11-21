package ua.rikutou.studiobackend.plugins.route.section

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.section.SectionDataSource

fun Route.deleteSection() {
    authenticate {
        delete("sections/{sectionId}") {
            val sectionDataSource by application.inject<SectionDataSource>()

            val sectionId = call.parameters["sectionId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Section Id not found"
                    )
                )
                return@delete
            }

            sectionDataSource.deleteSection(sectionId)
            call.respond(
                status = HttpStatusCode.OK,
                message = "Section deleted"
            )
        }
    }
}