package ua.rikutou.studiobackend.plugins.route.equipment

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.equipment.EquipmentDataSource

fun Route.deleteEquipment() {
    authenticate {
        delete("equipment/{equipmentId}") {
            val equipmentDataSource by application.inject<EquipmentDataSource>()

            val equipmentId = call.parameters["equipmentId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Equipment Id not found"
                    )
                )
                return@delete
            }

            equipmentDataSource.deleteById(equipmentId = equipmentId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Equipment deleted"
            )
        }
    }
}