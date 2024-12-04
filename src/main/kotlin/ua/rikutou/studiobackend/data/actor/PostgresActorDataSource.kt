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
import ua.rikutou.studiobackend.data.phone.Phone
import ua.rikutou.studiobackend.data.phone.PostgresPhoneDataSource
import ua.rikutou.studiobackend.data.phone.PostgresPhoneDataSource.Companion.phoneNumber
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.film.PostgresFilmDataSource as film
import java.sql.Connection
import java.sql.Statement

class PostgresActorDataSource(private val connection: Connection) : ActorDataSource {
    companion object {
        const val table = "actor"
        const val actorId = "actorId"
        const val name = "name"
        const val nickName = "nickName"
        const val role = "role"
        const val studioId = "studioId"

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
            af.actorId AS afactorid, af.filmid as affilmid, af.role AS roleInFilm,
            p.phoneNumber, p.phoneId AS pPhoneId
            FROM ${table}
            LEFT JOIN actor_film af ON actor.actorId = af.actorId
            LEFT JOIN film ON af.filmId = film.filmId
            LEFT JOIN actor_phone ap ON ap.actorId = actor.actorId
            LEFT JOIN phone p ON ap.phoneId = p.phoneId
            WHERE actor.studioId = ?
        """

        private const val getAllActorsFiltered = """
            SELECT 
            actor.actorId, actor.name, actor.nickname, actor.role AS actorRole, actor.studioId,
            film.filmId, film.title, film.genres, film.director, film.writer, film.date, film.budget,
            af.actorId AS afactorid, af.filmid as affilmid, af.role AS roleInFilm,
            p.phoneNumber, p.phoneId AS pPhoneId
            FROM ${table}
            LEFT JOIN actor_film af ON actor.actorId = af.actorId
            LEFT JOIN film ON af.filmId = film.filmId
            LEFT JOIN actor_phone ap ON ap.actorId = actor.actorId
            LEFT JOIN phone p ON ap.phoneId = p.phoneId
            WHERE actor.actorId = ?
            AND ($name ILIKE ? OR $nickName ILIKE ? OR $role ILIKE ?)
        """

        private const val getActorById = """
            SELECT 
            actor.actorId, actor.name, actor.nickname, actor.role, actor.studioId,
            film.filmId, film.title, film.genres, film.director, film.writer, film.date, film.budget,
            actor_film.role AS roleInFilm
            phone.phoneNumber
            FROM ${table}
            LEFT JOIN actor_film ON actor.actorId = actor_film.actorId
            LEFT JOIN film ON actor_film.filmId = film.filmId
            LEFT JOIN actor_phone ON actor_phone.actorId = actor.actorId
            LEFT JOIN phone ON actor_phone.phoneId = phone.phoneId
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

        private const val createActorToPhoneTable = """
            CREATE TABLE IF NOT EXISTS actor_phone (
            $actorId INTEGER
                REFERENCES ${table} (${actorId})
                ON DELETE CASCADE,
            phoneId INTEGER
                REFERENCES ${PostgresPhoneDataSource.table} (${PostgresPhoneDataSource.phoneId})
                ON DELETE CASCADE
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
        connection
            .createStatement()
            .executeUpdate(film.createTableFilm)

        connection
            .createStatement()
            .executeUpdate(createActorToPhoneTable)
        connection
            .createStatement()
            .executeUpdate(PostgresPhoneDataSource.createTablePhone)
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
        return@withContext mutableMapOf<Actor, ActorRelation>().apply {
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
                }
                else null

                val actorFilm = if(result.getString("roleInFilm")?.isNotEmpty() === true) {
                    ActorFilm(
                        actorId = result.getInt("afactorid"),
                        filmId = result.getInt("affilmid"),
                        role = result.getString("roleInFilm")
                    )
                }
                else null

                val phone = if(result.getString(phoneNumber)?.isNotEmpty() == true) {
                    Phone(
                        phoneId = result.getInt("pPhoneid"),
                        phoneNumber = result.getString(phoneNumber)
                    )
                }
                else null

                if(!containsKey(actor)) {
                    this[actor] = ActorRelation()
                }

                film?.let {
                    this[actor]?.films?.add(it)
                }
                actorFilm?.let {
                    this[actor]?.actorToFilms?.add(it)
                }
                phone?.let {
                    this[actor]?.phones?.add(it)
                }
            }

        }.map {
            it.key.copy(
                films = it.value.films.toList(),
                actorFilms = it.value.actorToFilms.toList(),
                phones = it.value.phones.toList(),
            )
        }
    }

    override suspend fun deleteById(actorId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteActor)
        statement.setInt(1, actorId)
        statement.execute()
    }
}