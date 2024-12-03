package ua.rikutou.studiobackend.data.film

import kotlinx.serialization.Serializable

@Serializable
data class Film(
    val filmId: Int? = null,
    val title: String,
    val genres: Array<Int>,
    val director: String,
    val writer: String,
    val date: Long,
    val budget: Float,
)
