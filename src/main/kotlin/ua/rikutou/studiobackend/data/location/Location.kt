package ua.rikutou.studiobackend.data.location

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.gallery.Gallery

@Serializable
data class Location(
    val locationId: Int? = null,
    val name: String,
    val address: String,
    val width: Float,
    val length: Float,
    val height: Float,
    val type: String,
    val studioId: Int,
    val rentPrice: Float,
    val images: List<Gallery>? = null,
)
