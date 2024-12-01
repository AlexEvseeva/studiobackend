package ua.rikutou.studiobackend.data.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.department.PostgresDepartmentDataSource
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import java.sql.Connection
import java.sql.Date
import java.sql.Statement
import javax.swing.plaf.nimbus.State

class PostgresTransportDataSource(private val connection: Connection) : TransportDataSource {
    companion object {
        private const val createTableTransport =
            """
                CREATE TABLE IF NOT EXISTS transport (
                    transportId SERIAL PRIMARY KEY,
                    type VARCHAR(100),
                    mark VARCHAR(100),
                    manufactureDate DATE,
                    seats INTEGER,
                    departmentId INTEGER REFERENCES ${PostgresDepartmentDataSource.table} (${PostgresDepartmentDataSource.departmentId}) 
                            ON DELETE CASCADE,
                    color VARCHAR(100),
                    technicalState VARCHAR(200)
                )
            """
        private const val insertTransport = "INSERT INTO transport (type, mark, manufactureDate, seats, departmentId, color, technicalState) VALUES (?, ?, ?, ?, ?, ?, ?)"
        private const val updateTransport = "UPDATE transport SET type = ?, mark = ?, manufactureDate = ?, seats = ?, departmentId = ?, color = ?, technicalState = ? WHERE transportId = ?"
        private const val getTransportById = "SELECT * FROM transport WHERE transportId = ?"
        private const val getAllTransport = "SELECT * FROM transport WHERE departmentId = ?"
        private const val getAllTransportFiltered = "SELECT * FROM transport WHERE departmentId = ? AND (type ILIKE ? OR mark ILIKE ? OR manufactureDate ILIKE ? OR seats ILIKE ? OR color ILIKE ? OR technicalState ILIKE ?))"
        private const val deleteTransport = "DELETE FROM transport WHERE transportId = ?"
        private const val getTransportFiltered = "SELECT * FROM transport WHERE departmentId = ? AND (type = ? OR mark = ? OR manufactureDate = ? OR technicalState = ?)"
    }

    init {
        connection.createStatement()
            .executeUpdate(createTableTransport)
    }

    override suspend fun insertUpdateTransport(transport: Transport): Int? = withContext(Dispatchers.IO) {
        val statement = if (transport.transportId != null) {
            connection.prepareStatement(updateTransport).apply {
                setString(1, transport.type)
                setString(2, transport.mark)
                setDate(3, Date(transport.manufactureDate))
                setInt(4, transport.seats)
                setInt(5, transport.departmentId)
                setString(6, transport.color)
                setString(7, transport.technicalState)
                setInt(8, transport.transportId)
            }
        } else {
            connection.prepareStatement(insertTransport, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, transport.type)
                setString(2, transport.mark)
                setDate(3, Date(transport.manufactureDate))
                setInt(4, transport.seats)
                setInt(5, transport.departmentId)
                setString(6, transport.color)
                setString(7, transport.technicalState)
            }
        }
        statement.executeUpdate()

        return@withContext if (transport.transportId != null) {
            transport.transportId
        } else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getTransportById(id: Int): Transport? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getTransportById)
        statement.setInt(1, id)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Transport (
                transportId = result.getInt("transportId"),
                type = result.getString("type"),
                mark = result.getString("mark"),
                manufactureDate = result.getDate("manufactureDate").time,
                seats = result.getInt("seats"),
                departmentId = result.getInt("departmentId"),
                color = result.getString("color"),
                technicalState = result.getString("technicalState"),
            )
        } else null
    }

    override suspend fun getAllTransport(departmentId: Int, search: String?): List<Transport> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(search?.let {
            getAllTransportFiltered
        } ?: getAllTransport)
        statement.setInt(1, departmentId)
        search?.let {
            val searchString = "%$search%"
            statement.setString(2, searchString)
            statement.setString(3, searchString)
            statement.setString(4, searchString)
            statement.setString(5, searchString)
            statement.setString(6, searchString)
            statement.setString(7, searchString)
        }

        val result = statement.executeQuery()
        return@withContext mutableListOf<Transport>().apply {
            while (result.next()) {
                add (
                    Transport(
                        transportId = result.getInt("transportId"),
                        type = result.getString("type"),
                        mark = result.getString("mark"),
                        manufactureDate = result.getDate("manufactureDate").time,
                        seats = result.getInt("seats"),
                        departmentId = result.getInt("departmentId"),
                        color = result.getString("color"),
                        technicalState = result.getString("technicalState"),
                    )
                )
            }
        }
    }

    override suspend fun deleteTransport(transportId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteTransport)
        statement.setInt(1, transportId)
        statement.execute()
    }
}