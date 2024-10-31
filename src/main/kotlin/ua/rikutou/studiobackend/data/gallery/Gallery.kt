package ua.rikutou.studiobackend.data.gallery

import kotlinx.serialization.Serializable

@Serializable
data class Gallery(
    val galleryId: Int,
    val url: String,
)
