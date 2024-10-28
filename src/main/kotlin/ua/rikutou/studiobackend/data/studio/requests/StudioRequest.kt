package ua.rikutou.studiobackend.data.studio.requests

import kotlinx.serialization.Serializable

@Serializable
data class StudioRequest(
    val studioId: Int? = null,
    val name: String,
    val address: String,
    val postIndex: String,
    val site: String,
    val youtube: String,
    val facebook: String,
)
