package ua.rikutou.studiobackend.plugins.route.equipment

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.equipment.EquipmentDataSource
import ua.rikutou.studiobackend.data.equipment.toEquipmentType
import ua.rikutou.studiobackend.data.transport.toTransportType

fun Route.getAllEquipment() {
    authenticate {
        get("equipment") {
            val equipmentDataSource by application.inject<EquipmentDataSource>()

            val studioId = call.parameters["studioId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio Id not found"
                    )
                )
                return@get
            }

            val search = call.parameters["search"]
            val type = call.runCatching { parameters["type"]?.toInt()?.toEquipmentType() }.getOrNull()

            val equipment = equipmentDataSource.getAllEquipment(studioId = studioId, search = search, equipmentType = type)
            if (equipment.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "No equipment found"
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