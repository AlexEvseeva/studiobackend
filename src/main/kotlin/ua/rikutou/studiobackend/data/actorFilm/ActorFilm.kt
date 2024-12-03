package ua.rikutou.studiobackend.data.actorFilm

import kotlinx.serialization.Serializable

@Serializable
data class ActorFilm(
    val actorId: Int,
    val filmId: Int,
    val role: String,
)
