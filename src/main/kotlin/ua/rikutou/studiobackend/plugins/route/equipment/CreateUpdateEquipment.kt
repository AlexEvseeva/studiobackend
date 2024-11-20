package ua.rikutou.studiobackend.plugins.route.equipment

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.equipment.Equipment
import ua.rikutou.studiobackend.data.equipment.EquipmentDataSource
import ua.rikutou.studiobackend.data.equipment.requests.EquipmentRequest

fun Route.createUpdateEquipment() {
    authenticate {
        post("/equipment") {

            val equipmentDataSource by application.inject<EquipmentDataSource>()

            val request = call.runCatching {
                this.receiveNullable<EquipmentRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Equipment's fields not found"
                    )
                )
                return@post
            }
            val equipment = Equipment(
                equipmentId = request.equipmentId,
                name = request.name,
                type = request.type,
                comment = request.comment,
                rentPrice = request.rentPrice,
                studioId = request.studioId,
            )
            val id = equipmentDataSource.insertUpdateEquipment(equipment)
            call.respond(
                status = HttpStatusCode.OK,
                message = equipment.copy(equipmentId = id)
            )
        }
    }
}