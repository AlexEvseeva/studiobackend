package ua.rikutou.studiobackend.data.statistic

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.actor.Actor
import ua.rikutou.studiobackend.data.equipment.Equipment
import ua.rikutou.studiobackend.data.location.Location
import ua.rikutou.studiobackend.data.transport.Transport

@Serializable
data class Statistic(
    val location: MinAvgMax? = null,
    val transport: MinAvgMax? = null,
    val equipment: MinAvgMax? = null,
    val incomeStructure: IncomeStructure? = null,
    val documentsTotal: Int? = null,
    val mostPopularLocations: List<Location>? = null,
    val mostPopularTransport: List<Transport>? = null,
    val mostPopularEquipment: List<Equipment>? = null,
    val mostPopularActor: Actor? = null,
)
