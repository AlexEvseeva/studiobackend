package ua.rikutou.studiobackend.plugins.route.department

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.department.DepartmentDataSource

fun Route.getAllDepartments() {
    authenticate {
        get("departments") {
            val departmentDataSource by application.inject<DepartmentDataSource>()
            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found."
                    )
                )
                return@get
            }
            val search = call.parameters["search"]

            val departments = departmentDataSource.getAllDepartments(studioId = studioId, search = search)
            if (departments.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "No department found."
                    )
                )
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = departments
            )
        }
    }
}