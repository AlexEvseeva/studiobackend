package ua.rikutou.studiobackend.data.statistic

import kotlinx.serialization.Serializable

@Serializable
data class IncomeStructure(
    val byLocation: Float? = null,
    val byTransport: Float? = null,
    val byEquipment: Float? = null,
    val total: Float? = null,
)
