package ua.rikutou.studiobackend.data.actor

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.actorFilm.ActorFilm
import ua.rikutou.studiobackend.data.film.Film
import ua.rikutou.studiobackend.data.phone.Phone

@Serializable
data class Actor(
    val actorId: Int? = null,
    val name: String,
    val nickName: String? = null,
    val role: String? = null,
    val studioId: Int,
    val films: List<Film>? = null,
    val actorFilms: List<ActorFilm>? = null,
    val phones: List<Phone>? = null,
)
