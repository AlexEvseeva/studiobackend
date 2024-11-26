package ua.rikutou.studiobackend.data.transport

import java.sql.Connection
import java.sql.Statement

class PostgresTransportDataSource(private val connection: Connection) : TransportDataSource {
    companion object {
        private const val createTableTransport = "CREATE TABLE IF NOT EXISTS transport (transportId SERIAL PRIMARY KEY, type VARCHAR(100), mark VARCHAR(100), manufactureDate DATE, seats INTEGER, departmentId INTEGER, color VARCHAR(100), technicalState VARCHAR(200))"
        private const val insertTransport = "INSERT INTO transport (type, mark, manufactureDate, seats, departmentId, color, technicalState) VALUES (?, ?, ?, ?, ?, ?, ?)"
        private const val updateTransport = "UPDATE transport SET type = ?, mark = ?, manufactureDate = ?, seats = ?, departmentId = ?, manufactureDate = ? WHERE transportId = ?"
        private const val getTransportById = "SELECT * FROM transport WHERE transportId = ?"
        private const val getAllTransport = "SELECT * FROM transport WHERE departmentId = ?"
        private const val deleteTransport = "DELETE FROM transport WHERE transportId = ?"
        private const val getTransportFiltered = "SELECT * FROM transport WHERE departmentId = ? AND (type = ? OR mark = ? OR manufactureDate = ? OR technicalState = ?)"
    }

    override suspend fun insertUpdateTransport(transport: Transport): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTransportById(id: Int): Transport? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTransport(deaprtmentId: Int, search: String): List<Transport> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTransport(transportId: Int) {
        TODO("Not yet implemented")
    }
}