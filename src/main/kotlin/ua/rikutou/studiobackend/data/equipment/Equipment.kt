package ua.rikutou.studiobackend.data.equipment

import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    val equipmentId: Int? = null,
    val name: String,
    val type : String,
    val comment: String,
    val rentPrice: Float,
    val studioId: Int,
)
