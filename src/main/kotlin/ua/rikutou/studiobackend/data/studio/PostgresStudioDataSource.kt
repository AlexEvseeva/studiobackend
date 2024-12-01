package ua.rikutou.studiobackend.data.studio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresStudioDataSource(private val connection: Connection) : StudioDataSource {
    companion object {
        const val table = "studio"
        const val studioId = "studioId"
        const val createTableStudio = "CREATE TABLE IF NOT EXISTS studio (studioId SERIAL PRIMARY KEY, name VARCHAR(70), address VARCHAR(200), postIndex CHAR(5), site VARCHAR(100), youtube VARCHAR(100), facebook VARCHAR(100))"
        const val insertStudio = "INSERT INTO studio (name, address, postIndex, site, youtube, facebook) VALUES (?, ?, ?, ?, ?, ?)"
        const val getStudioById = "SELECT * FROM studio WHERE studioId = ? LIMIT 1"
        const val getStudioByName = "SELECT * FROM studio WHERE name = ?"
        const val updateStudio = "UPDATE studio SET name = ?, address = ?, postIndex = ?, site = ?, youtube = ?, facebook = ? WHERE studioId = ?"
        const val deleteStudio = "DELETE FROM studio WHERE studioId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableStudio)
    }
    override suspend fun insertStudio(studio: Studio): Int? = withContext(Dispatchers.IO) {
        getStudioByName(name = studio.name)?.let {
            return@withContext null
        }
        val statement = connection.prepareStatement(insertStudio, Statement.RETURN_GENERATED_KEYS)
        statement.apply {
            setString(1, studio.name)
            setString(2, studio.address)
            setString(3, studio.postIndex)
            setString(4, studio.site)
            setString(5, studio.youtube)
            setString(6, studio.facebook)
        }.executeUpdate()

        return@withContext if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun updateStudio(studio: Studio) = withContext(Dispatchers.IO) {
        studio.studioId?.let {
            connection.prepareStatement(updateStudio).apply {
                setString(1, studio.name)
                setString(2, studio.address)
                setString(3, studio.postIndex)
                setString(4, studio.site)
                setString(5, studio.youtube)
                setString(6, studio.facebook)
                setInt(7, studio.studioId)
            }.executeUpdate()
        }
        return@withContext
    }

    override suspend fun getStudioById(studioId: Int): Studio? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getStudioById)
        statement.setInt(1, studioId)
        val result = statement.executeQuery()

        return@withContext if(result.next()) {
            Studio (
                studioId = result.getInt("studioId"),
                name = result.getString("name"),
                address = result.getString("address"),
                postIndex = result.getString("postIndex"),
                site = result.getString("site"),
                youtube = result.getString("youtube"),
                facebook = result.getString("facebook"),
            )
        } else null
    }

    override suspend fun getStudioByName(name: String): Studio? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getStudioByName)
        statement.setString(1, name)
        val result = statement.executeQuery()

        return@withContext if(result.next()) {
            Studio (
                studioId = result.getInt("studioId"),
                name = result.getString("name"),
                address = result.getString("address"),
                postIndex = result.getString("postIndex"),
                site = result.getString("site"),
                youtube = result.getString("youtube"),
                facebook = result.getString("facebook"),
            )
        } else null
    }

    override suspend fun deleteStudioById(studioId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteStudio)
        statement.setInt(1, studioId)
        statement.execute()
    }
}