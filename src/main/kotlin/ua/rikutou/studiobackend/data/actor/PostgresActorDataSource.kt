package ua.rikutou.studiobackend.data.actor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.film.Film
import ua.rikutou.studiobackend.data.actorFilm.ActorFilm
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource.Companion.budget
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource.Companion.date
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource.Companion.director
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource.Companion.title
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource.Companion.writer
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource as film
import java.sql.Connection
import java.sql.Statement

class PostgresActorDataSource(private val connection: Connection) : ActorDataSource {
    companion object {
        private const val table = "actor"
        private const val actorId = "actorId"
        private const val name = "name"
        private const val nickName = "nickName"
        private const val role = "role"
        private const val studioId = "studioId"

        private const val createTableActor =
            """
                CREATE TABLE IF NOT EXISTS $table (
                $actorId SERIAL PRIMARY KEY,
                $name VARCHAR(255) NOT NULL,
                $nickName VARCHAR(255),
                $role VARCHAR(255),
                $studioId INTEGER
                    REFERENCES ${PostgresStudioDataSource.table} (${PostgresStudioDataSource.studioId})
                    ON DELETE CASCADE
                )
            """
        private const val insertActor = "INSERT INTO $table ($name, $nickName, $role, $studioId) VALUES (?, ?, ?, ?)"
        private const val updateActor = "UPDATE $table SET $name = ?, $nickName = ?, $role = ?, $studioId = ? WHERE $actorId = ?"
        private const val deleteActor = "DELETE FROM $table WHERE $actorId = ?"
        private const val getAllActors = """
            SELECT 
            actor.actorId, actor.name, actor.nickname, actor.role AS actorRole, actor.studioId,
            film.filmId, film.title, film.genres, film.director, film.writer, film.date, film.budget,
            af.actorId AS afactorid, af.filmid as affilmid, af.role AS roleInFilm
            FROM ${table}
            LEFT JOIN actor_film af ON actor.actorId = af.actorId
            LEFT JOIN film ON af.filmId = film.filmId
            WHERE actor.studioId = ?
        """

        private const val getAllActorsFiltered = """
            SELECT 
            actor.actorId, actor.name, actor.nickname, actor.role AS actorRole, actor.studioId,
            film.filmId, film.title, film.genres, film.director, film.writer, film.date, film.budget,
            af.actorId AS afactorid, af.filmid as affilmid, af.role AS roleInFilm
            FROM ${table}
            LEFT JOIN actor_film af ON actor.actorId = af.actorId
            LEFT JOIN film ON af.filmId = film.filmId
            WHERE actor.actorId = ?
            AND ($name ILIKE ? OR $nickName ILIKE ? OR $role ILIKE ?)
        """

        private const val getActorById = """
            SELECT 
            actor.actorId, actor.name, actor.nickname, actor.role, actor.studioId,
            film.filmId, film.title, film.genres, film.director, film.writer, film.date, film.budget,
            actor_film.role AS roleInFilm
            FROM ${table}
            LEFT JOIN actor_film ON actor.actorId = actor_film.actorId
            LEFT JOIN film ON actor_film.filmId = film.filmId
            WHERE actor.actorId = ?
        """

        private const val createActorToFilmTable = """
            CREATE TABLE IF NOT EXISTS actor_film (
            $actorId INTEGER
                REFERENCES ${table} (${actorId})
                ON DELETE CASCADE,
            filmId INTEGER
                REFERENCES ${film.table} (${film.filmId})
                ON DELETE CASCADE,
            role VARCHAR(250) NOT NULL
            )
        """
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableActor)
        connection
            .createStatement()
            .executeUpdate(createActorToFilmTable)
        connection.createStatement()
            .executeUpdate(film.createTableFilm)
    }

    override suspend fun insertUpdateActors(actor: Actor): Int? = withContext(Dispatchers.IO) {
        val statement = if (actor.actorId != null) {
            connection.prepareStatement(updateActor).apply {
                setString(1, actor.name)
                setString(2, actor.nickName)
                setString(3, actor.role)
                setInt(4, actor.studioId)
                setInt(5, actor.actorId)
            }
        } else {
            connection.prepareStatement(insertActor, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, actor.name)
                setString(2, actor.nickName)
                setString(3, actor.role)
                setInt(4, actor.studioId)
            }
        }
        statement.executeUpdate()
        return@withContext if (actor.actorId != null) {
            actor.actorId
        } else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getActorById(id: Int): Actor? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getActorById)
        statement.setInt(1, id)
        val result = statement.executeQuery()

        return@withContext mutableMapOf<Actor, Pair<MutableSet<Film>, MutableSet<ActorFilm>>>().apply {
            while (result.next()) {
                val actor = Actor(
                    actorId = result.getInt(actorId),
                    name = result.getString(name),
                    nickName = result.getString(nickName),
                    role = result.getString(role),
                    studioId = result.getInt(studioId)
                )

                val film = if(result.getInt(film.filmId) != 0) {
                    val genres = result.getArray("genres")
                    val intArray = genres.array as Array<Int>
                    Film (
                        filmId = result.getInt(film.filmId),
                        title = result.getString(title),
                        genres = intArray,
                        director = result.getString(director),
                        writer = result.getString(writer),
                        date = result.getDate(date).time,
                        budget = result.getFloat(budget)
                    )
                } else null

                val actorFilm = if(film?.filmId != null && result.getInt(film.filmId) != 0) {
                    ActorFilm(
                        actorId = result.getInt(actorId),
                        filmId = result.getInt(film.filmId),
                        role = result.getString("roleInFilm")
                    )
                } else null

                if(!containsKey(actor)) {
                    this[actor] = Pair(mutableSetOf<Film>(), mutableSetOf<ActorFilm>())
                }

                film?.let {
                    this[actor]?.first?.add(it)
                }
                actorFilm?.let {
                    this[actor]?.second?.add(it)
                }
            }

        }.map {
            it.key.copy(
                films = it.value.first.toList(),
                actorFilms = it.value.second.toList()
            )
        }.first()
    }

    override suspend fun getAllActors(studioId: Int, search: String?): List<Actor> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(search?.let {
            getAllActorsFiltered
        } ?: getAllActors)
        statement.setInt(1, studioId)
        search?.let {
            val searchString = "%$search%"
            statement.setString(2, searchString)
            statement.setString(3, searchString)
            statement.setString(4, searchString)
        }

        val result = statement.executeQuery()
        return@withContext mutableMapOf<Actor, Pair<MutableSet<Film>, MutableSet<ActorFilm>>>().apply {
            while (result.next()) {
                val actor = Actor(
                    actorId = result.getInt(actorId),
                    name = result.getString(name),
                    nickName = result.getString(nickName),
                    role = result.getString("actorRole"),
                    studioId = result.getInt(Companion.studioId)
                )

                val film = if(result.getInt(film.filmId) != 0) {
                    val genres = result.getArray("genres")
                    val intArray = genres.array as Array<Int>
                    Film (
                        filmId = result.getInt(film.filmId),
                        title = result.getString(title),
                        genres = intArray,
                        director = result.getString(director),
                        writer = result.getString(writer),
                        date = result.getDate(date).time,
                        budget = result.getFloat(budget)
                    )
                } else null

                val actorFilm = if(result.getString("roleInFilm")?.isNotEmpty() === true) {
                    ActorFilm(
                        actorId = result.getInt("afactorid"),
                        filmId = result.getInt("affilmid"),
                        role = result.getString("roleInFilm")
                    )
                } else null

                if(!containsKey(actor)) {
                    this[actor] = Pair(mutableSetOf<Film>(), mutableSetOf<ActorFilm>())
                }

                film?.let {
                    this[actor]?.first?.add(it)
                }
                actorFilm?.let {
                    this[actor]?.second?.add(it)
                }
            }

        }.map {
            it.key.copy(
                films = it.value.first.toList(),
                actorFilms = it.value.second.toList()
            )
        }
    }

    override suspend fun deleteById(actorId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteActor)
        statement.setInt(1, actorId)
        statement.execute()
    }
}