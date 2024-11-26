package ua.rikutou.studiobackend.data.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.user.responses.StudioUser
import java.sql.Connection
import java.sql.Statement


class PostgresUserDataSource(private val connection: Connection) : UserDataSource {
    companion object {
        private const val createTableUsers = "CREATE TABLE IF NOT EXISTS users (userId SERIAL PRIMARY KEY, name VARCHAR(200), password VARCHAR(200), salt VARCHAR(200), studioId int)"
        private const val getUserByUserName = "SELECT * FROM users WHERE name = ?"
        private const val getUserByUserId = "SELECT * FROM users WHERE userId = ?"
        private const val insertUser = "INSERT INTO users (name, password, salt, studioId) VALUES (?, ?, ?, ?)"
        private const val updateUserStudioId = "UPDATE users SET studioId = ? WHERE userId = ?"
        private const val getUsersWithStudioIdAndCandidates = "SELECT * FROM users WHERE studioId = ? OR studioId < 0"
        private const val deleteUser = "DELETE FROM users WHERE userId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableUsers)
    }

    override suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getUserByUserId)
        statement.setInt(1, userId)
        val result = statement.executeQuery()

        return@withContext if(result.next()) {
            User(
                userId = result.getInt("userId"),
                name = result.getString("name"),
                password = result.getString("password"),
                salt = result.getString("salt"),
                studioId = result.getInt("studioId")
            )
        } else null
    }

    override suspend fun getUserByUserName(name: String): User? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getUserByUserName)
        statement.setString(1, name)
        val result = statement.executeQuery()


        return@withContext if(result.next()) {
            println("studioId: ${ result.getInt("studioId")}")
            User(
                userId = result.getInt("userId"),
                name = result.getString("name"),
                password = result.getString("password"),
                salt = result.getString("salt"),
                studioId = result.getInt("studioId")
            )
        } else null

    }

    override suspend fun insertUser(user: User): Boolean = withContext(Dispatchers.IO) {
        getUserByUserName(name = user.name)?.let {
            return@withContext false
        }

        val statement = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)
        statement.apply {
            setString(1, user.name)
            setString(2, user.password)
            setString(3, user.salt)
            setInt(4, -1)
        }.executeUpdate()

        return@withContext statement.generatedKeys.next()
    }

    override suspend fun updateUser(userId: Int, studioId: Int): Boolean = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(updateUserStudioId)
        val result = statement.apply {
            setInt(1, studioId)
            setInt(2, userId)
        }.executeUpdate()

        return@withContext result > 0
    }

    override suspend fun getStudioUsersAndCandidates(studioId: Int): List<StudioUser> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getUsersWithStudioIdAndCandidates)
        statement.setInt(1, studioId)
        val result = statement.executeQuery()

        return@withContext mutableListOf<StudioUser>().apply {
            while (result.next()) {
                add(
                    StudioUser(
                        userId = result.getInt("userId"),
                        userName = result.getString("name"),
                        studioId = result.getInt("studioId")
                    )
                )
            }
        }

    }

    override suspend fun deleteUserById(userId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteUser)
        statement.setInt(1, userId)
        statement.execute()
    }

}