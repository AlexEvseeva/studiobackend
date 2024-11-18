package ua.rikutou.studiobackend.plugins.route.department

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.h2.api.ErrorCode
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.department.DepartmentDataSource

fun Route.getDepartmentById() {
    authenticate {
        get("departmentById") {

            val departmentDataSource by application.inject<DepartmentDataSource>()

            val departmentId = call.parameters["departmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Department id not found"
                    )
                )
                return@get
            }

            val department = departmentDataSource.getDepartmentById(id = departmentId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "Department not found"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = department
            )
        }
    }
}