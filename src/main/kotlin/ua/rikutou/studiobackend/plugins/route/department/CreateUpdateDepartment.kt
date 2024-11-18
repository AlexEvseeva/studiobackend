package ua.rikutou.studiobackend.plugins.route.department

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.department.Department
import ua.rikutou.studiobackend.data.department.DepartmentDataSource
import ua.rikutou.studiobackend.data.department.requests.DepartmentRequest
import kotlin.concurrent.thread

fun Route.createUpdateDepartment() {
    authenticate {
        post("departments") {

            val departmentDateSource by application.inject<DepartmentDataSource>()

            val request = call.runCatching {
                this.receiveNullable<DepartmentRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Department's fields not found"
                    )
                )
                return@post
            }
            val department = Department(
                departmentId = request.departmentId,
                type = request.type,
                workHours = request.workHours,
                contactPerson = request.contactPerson,
                studioId = request.studioId,
            )
            val id = departmentDateSource.insertUpdateDepartment(department)
            call.respond(status = HttpStatusCode.OK, message = department.copy(departmentId = id))
        }
    }
}