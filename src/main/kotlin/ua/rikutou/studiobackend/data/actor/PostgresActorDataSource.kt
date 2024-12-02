package ua.rikutou.studiobackend.data.actor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
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
        private const val getAllActors = "SELECT * FROM $table WHERE $studioId = ?"
        private const val getAllActorsFiltered = "SELECT * FROM $table WHERE $studioId = ? AND ($name ILIKE ? OR $nickName ILIKE ? OR $role ILIKE ?)"
        private const val getActorById = "SELECT * FROM $table WHERE $actorId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableActor)
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

        return@withContext if (result.next()) {
            Actor(
                actorId = result.getInt(actorId),
                name = result.getString(name),
                nickName = result.getString(nickName),
                role = result.getString(role),
                studioId = result.getInt(studioId)
            )
        } else null
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
        return@withContext mutableListOf<Actor>().apply {
            while (result.next()) {
                add(
                    Actor(
                        actorId = result.getInt(actorId),
                        name = result.getString(name),
                        nickName = result.getString(nickName),
                        role = result.getString(role),
                        studioId = result.getInt(Companion.studioId)
                    )
                )
            }
        }
    }

    override suspend fun deleteById(actorId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteActor)
        statement.setInt(1, actorId)
        statement.execute()
    }
}