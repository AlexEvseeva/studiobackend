package ua.rikutou.studiobackend.data.reportLocation

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.location.Location

@Serializable
data class ReportLocation(
    val locations: List<Location>? = null,
    val locationsCount: Int? = null,
    val locationsCountTotal: Int? = null,
)
