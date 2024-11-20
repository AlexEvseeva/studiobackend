package ua.rikutou.studiobackend.plugins.route.department

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.department.DepartmentDataSource

fun Route.deleteDepartment() {
    authenticate {
        delete("departments/{departmentId}") {
            val departmentDataSource by application.inject<DepartmentDataSource>()

            val departmentId = call.parameters["departmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Department Id not found"
                    )
                )
                return@delete
            }

            departmentDataSource.deleteDepartment(id =  departmentId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Department deleted"
            )
        }
    }
}