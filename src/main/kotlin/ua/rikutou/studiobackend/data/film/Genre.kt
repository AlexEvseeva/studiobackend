package ua.rikutou.studiobackend.data.film

import kotlinx.serialization.SerialName

enum class Genre {
    @SerialName("0") Horror,
    @SerialName("1") Adventure,
    @SerialName("2") Comedy,
    @SerialName("3") Romantic,
    @SerialName("4") Blockbuster;

    fun fromGenre(): Int =
        when(this) {
            Horror -> 0
            Adventure -> 1
            Comedy -> 2
            Romantic -> 3
            Blockbuster -> 4
        }
}

fun Int.toGenre(): Genre =
    when(this) {
        0 -> Genre.Horror
        1 -> Genre.Adventure
        2 -> Genre.Comedy
        3 -> Genre.Romantic
        4 -> Genre.Blockbuster
        else -> throw IllegalArgumentException("Unknown Genre type: $this")
    }