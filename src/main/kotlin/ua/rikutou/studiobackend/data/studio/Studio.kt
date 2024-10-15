package ua.rikutou.studiobackend.data.studio

import kotlinx.serialization.Serializable

@Serializable
data class Studio (
    val studioId: Int? = null,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
)