package ua.rikutou.studiobackend.data.location.requests

import kotlinx.serialization.Serializable

@Serializable
data class LocationRequest(
    val locationId: Int? = null,
    val name: String,
    val address: String,
    val width: Float,
    val length: Float,
    val height: Float,
    val type: String,
    val studioId: Int,
    val rentPrice: Float,
)
