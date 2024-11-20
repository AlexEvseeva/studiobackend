package ua.rikutou.studiobackend.data.equipment.requests

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRequest(
    val equipmentId: Int? = null,
    val name: String,
    val type : String,
    val comment: String,
    val rentPrice: Float,
    val studioId: Int,
)
