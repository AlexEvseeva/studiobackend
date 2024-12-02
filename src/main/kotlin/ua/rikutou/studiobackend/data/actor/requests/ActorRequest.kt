package ua.rikutou.studiobackend.data.actor.requests

import kotlinx.serialization.Serializable

@Serializable
data class ActorRequest(
    val actorId: Int? = null,
    val name: String,
    val nickName: String? = null,
    val role: String? = null,
    val studioId: Int,
)
