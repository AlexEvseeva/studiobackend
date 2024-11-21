package ua.rikutou.studiobackend.plugins.route.section

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.section.Section
import ua.rikutou.studiobackend.data.section.SectionDataSource
import ua.rikutou.studiobackend.data.section.requests.SectionRequest

fun Route.createUpdateSection() {
    authenticate {
        post("sections") {

            val sectionDataSource by application.inject<SectionDataSource>()

            val request = call.runCatching {
                this.receiveNullable<SectionRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Section Id not found"
                    )
                )
                return@post
            }

            val section = Section(
                sectionId = request.sectionId,
                title = request.title,
                address = request.address,
                internalPhoneNumber = request.internalPhoneNumber,
                departmentId = request.departmentId,
            )
            val id = sectionDataSource.insertUpdateSection(section)
            call.respond(
                status = HttpStatusCode.OK,
                message = section.copy(sectionId = id)
            )
        }
    }
}