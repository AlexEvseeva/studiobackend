package ua.rikutou.studiobackend.data.film

interface FilmDataSource {
    suspend fun insertUpdatedFilm(film: Film): Int?
    suspend fun getFilmById(filmId: Int): Film?
    suspend fun deleteFilm(filmId: Int)
}