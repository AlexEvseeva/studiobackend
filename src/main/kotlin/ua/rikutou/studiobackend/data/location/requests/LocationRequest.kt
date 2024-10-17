package ua.rikutou.studiobackend.data.location.requests

import kotlinx.serialization.Serializable

@Serializable
data class LocationRequest(
    val name: String,
    val address: String,
    val width: Float,
    val length: Float,
    val height: Float,
    val type: String,
    val rentPrice: Float,
)
