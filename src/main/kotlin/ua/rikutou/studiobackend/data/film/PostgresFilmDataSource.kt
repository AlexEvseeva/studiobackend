package ua.rikutou.studiobackend.data.film

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Date
import java.sql.Statement

class PostgresFilmDataSource(private val connection: Connection) : FilmDataSource {
    companion object {
        const val table = "film"
        const val filmId = "filmId"
        const val title = "title"
        const val genres = "genres"
        const val director = "director"
        const val writer = "writer"
        const val date = "date"
        const val budget = "budget"

        const val createTableFilm =
            """
                CREATE TABLE IF NOT EXISTS $table (
                $filmId INTEGER PRIMARY KEY,
                $title TEXT NOT NULL,
                $genres INTEGER[],
                $director TEXT NOT NULL,
                $writer TEXT NOT NULL,
                $date DATE NOT NULL,
                $budget INTEGER NOT NULL
                )
            """
        private const val insertFilm = "INSERT INTO $table ($title, $genres, $director, $writer, $date, $budget) VALUES (?,?,?,?,?,?))"
        private const val updateFilm = "UPDATE $table SET $title = ?, $genres = ?, $director = ?, $writer = ?, $date = ?, $budget = ? WHERE $filmId = ?"
        private const val getFilmById = "SELECT * FROM $table WHERE $filmId = ?"
        private const val deleteFilm = "DELETE FROM $table WHERE $filmId = ?"
    }

    init {
        connection.createStatement()
            .executeUpdate(createTableFilm)
    }
    override suspend fun insertUpdatedFilm(film: Film): Int? = withContext(Dispatchers.IO){
        val statement = if (film.filmId != null) {
            connection.prepareStatement(updateFilm).apply {
                setString(1, film.title)
                setArray(2, connection.createArrayOf("INTEGER", film.genres))
                setString(3, film.director)
                setString(4, film.writer)
                setDate(5, Date(film.date))
                setFloat(6, film.budget)
                setInt(7, film.filmId)
            }
        } else {
            connection.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, film.title)
                setArray(2, connection.createArrayOf("INTEGER", film.genres))
                setString(3, film.director)
                setString(4, film.writer)
                setDate(5, Date(film.date))
                setFloat(6, film.budget)
            }
        }
        statement.executeUpdate()

        return@withContext if (film.filmId != null) {
            film.filmId
        }
        else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        }
        else null
    }

    override suspend fun getFilmById(filmId: Int): Film? = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(getFilmById)
        statement.setInt(1, filmId)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            val genres = result.getArray("genres")
            val intArray = genres.array as Array<Int>
            Film (
                filmId = result.getInt(filmId),
                title = result.getString(title),
                genres = intArray,
                director = result.getString(director),
                writer = result.getString(writer),
                date = result.getDate(date).time,
                budget = result.getFloat(budget)
            )
        } else null
    }

    override suspend fun deleteFilm(filmId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteFilm)
        statement.setInt(1, filmId)
        statement.execute()
    }


}