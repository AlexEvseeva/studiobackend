package ua.rikutou.studiobackend.data.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement


class PostgresUserDataSource(private val connection: Connection) : UserDataSource {
    companion object {
        private const val createTableUsers = "CREATE TABLE IF NOT EXISTS users (userId SERIAL PRIMARY KEY, name VARCHAR(200), password VARCHAR(200), salt VARCHAR(200))"
        private const val getUserByUserName = "SELECT * FROM users WHERE name = ?"
        private const val insertUser = "INSERT INTO users (name, password, salt) VALUES (?, ?, ?)"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableUsers)
    }
    override suspend fun getUserByUserName(name: String): User? = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(getUserByUserName)
        statement.setString(1, name)
        val result = statement.executeQuery()


        return@withContext if(result.next()) {
            User(
                userId = result.getInt("userId"),
                name = result.getString("name"),
                password = result.getString("password"),
                salt = result.getString("salt")
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
        }.executeUpdate()

        return@withContext statement.generatedKeys.next()
    }

}