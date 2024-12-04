package ua.rikutou.studiobackend.data.phone

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource
import ua.rikutou.studiobackend.data.department.PostgresDepartmentDataSource
import java.sql.Connection
import java.sql.Statement

class PostgresPhoneDataSource(private val connection: Connection) : PhoneDataSource {
    companion object {
        const val table = "phone"
        const val phoneId = "phoneId"
        const val phoneNumber = "phoneNumber"

        const val createTablePhone = """
            CREATE TABLE IF NOT EXISTS $table (
            $phoneId SERIAL PRIMARY KEY,
            $phoneNumber VARCHAR(12) NOT NULL
            )
        """
        private const val insertPhone = "INSERT INTO $table ($phoneId, $phoneNumber) VALUES (?, ?)"
        private const val deletePhone = "DELETE FROM $table WHERE $phoneId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTablePhone)
    }

    override suspend fun insertPhone(phone: Phone): Int? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(insertPhone, Statement.RETURN_GENERATED_KEYS).apply {
            setInt(1, phone.phoneId)
            setString(2, phone.phoneNumber)
        }
        statement.executeUpdate()
        return@withContext if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun deletePhoneById(phoneId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deletePhone)
        statement.setInt(1, phoneId)
        statement.execute()
    }
}