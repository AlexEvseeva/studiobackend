package ua.rikutou.studiobackend.data.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresEmailDataSource(private val connection: Connection) : EmailDataSource {
    companion object {
        const val table = "email"
        const val emailId = "emailId"
        const val email = "email"

        const val createTableEmail = """
            CREATE TABLE IF NOT EXISTS $table (
            $emailId SERIAL PRIMARY KEY,
            $email VARCHAR(100) NOT NULL
            )
        """
        private const val insertEmail = "INSERT INTO $table ($emailId, $email) VALUES(?, ?)"
        private const val deleteEmail = "DELETE FROM $table WHERE emailId = $emailId"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableEmail)
    }

    override suspend fun insertEmail(email: Email): Int? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(insertEmail, Statement.RETURN_GENERATED_KEYS).apply {
            setInt(1, email.emailId)
            setString(2, email.email)
        }
        statement.executeUpdate()
        return@withContext if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun deleteEmail(emailId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteEmail)
        statement.setInt(1, emailId)
        statement.execute()
    }
}