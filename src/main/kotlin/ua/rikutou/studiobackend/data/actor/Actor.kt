package ua.rikutou.studiobackend.data.actor

import kotlinx.serialization.Serializable

@Serializable
data class Actor(
    val actorId: Int? = null,
    val name: String,
    val nickName: String? = null,
    val role: String? = null,
    val studioId: Int,
)
