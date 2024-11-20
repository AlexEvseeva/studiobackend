package ua.rikutou.studiobackend.plugins.route.equipment

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.equipment.EquipmentDataSource

fun Route.getEquipmentById() {
    authenticate {
        get("/equipmentById") {

            val equipmentDataSource by application.inject<EquipmentDataSource>()

            val equipmentId = call.parameters["equipmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Equipment Id not found"
                    )
                )
                return@get
            }

            val equipment = equipmentDataSource.getEquipmentById(equipmentId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error (
                        code = HttpStatusCode.NotFound.value,
                        message = "Equipment not found"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = equipment
            )
        }
    }
}