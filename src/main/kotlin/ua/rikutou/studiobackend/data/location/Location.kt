package ua.rikutou.studiobackend.data.location

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val locationId: Int? = null,
    val name: String,
    val address: String,
    val width: Float,
    val length: Float,
    val height: Float,
    val type: String,
    val studioId: Int? = null,
    val rentPrice: Float,
)
