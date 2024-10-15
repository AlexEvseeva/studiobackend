package ua.rikutou.studiobackend.data.studio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresStudioDataSource(private val connection: Connection) : StudioDataSource {
    companion object {
        private const val createTableStudio = "CREATE TABLE IF NOT EXISTS studio (studioId SERIAL PRIMARY KEY, name VARCHAR(70), address VARCHAR(200), phone CHAR(13), email VARCHAR(70))"
        private const val insertStudio = "INSERT INTO studio (name, address, phone, email) VALUES (?, ?, ?, ?)"
        private const val getStudioById = "SELECT * FROM studio WHERE studioId = ? LIMIT 1"
        private const val getStudioByName = "SELECT * FROM studio WHERE name = ?"
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
            setString(3, studio.phone)
            setString(4, studio.email)
        }.executeUpdate()

        return@withContext if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
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
                phone = result.getString("phone"),
                email = result.getString("email")
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
                phone = result.getString("phone"),
                email = result.getString("email")
            )
        } else null
    }
}