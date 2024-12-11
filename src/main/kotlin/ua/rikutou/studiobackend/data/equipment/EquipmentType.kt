package ua.rikutou.studiobackend.data.equipment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EquipmentType {
    @SerialName("0") Camera,
    @SerialName("1") Tripod,
    @SerialName("2") Flash;

    fun toDb() =
        when(this) {
            Camera -> 0
            Tripod -> 1
            Flash -> 2
        }
}

fun Int.toEquipmentType(): EquipmentType =
    when(this) {
        0 -> EquipmentType.Camera
        1 -> EquipmentType.Tripod
        2 -> EquipmentType.Flash
        else -> throw IllegalArgumentException("EquipmentType $this is not a valid equipmentType")
    }