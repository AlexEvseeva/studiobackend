package ua.rikutou.studiobackend.plugins.route.section

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.section.SectionDataSource

fun Route.getAllSections() {
    authenticate {
        get("sections") {
            val sectionDataSource by application.inject<SectionDataSource>()
            val departmentId = call.parameters["departmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Department Id not found"
                    )
                )
                return@get
            }
            val search = call.parameters["search"]

            val sections = sectionDataSource.getAllSections(departmentId = departmentId, search = search)
            if (sections.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "No sections found"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = sections
            )
        }
    }
}