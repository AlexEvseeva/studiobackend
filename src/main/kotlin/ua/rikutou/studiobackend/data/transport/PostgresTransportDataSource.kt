package ua.rikutou.studiobackend.data.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.department.PostgresDepartmentDataSource
import java.sql.Connection
import java.sql.Date
import java.sql.Statement
import java.text.SimpleDateFormat

class PostgresTransportDataSource(private val connection: Connection) : TransportDataSource {
    companion object {
        const val table = "transport"
        const val transportId = "transportId"
        const val type = "type"
        const val mark = "mark"
        const val manufactureDate = "manufactureDate"
        const val seats = "seats"
        const val departmentId = "departmentId"
        const val color = "color"
        const val technicalState = "technicalState"
        const val rentPrice = "rentPrice"

        const val createTableTransport =
            """
                CREATE TABLE IF NOT EXISTS transport (
                    transportId SERIAL PRIMARY KEY,
                    type INTEGER,
                    mark VARCHAR(100),
                    manufactureDate DATE,
                    seats INTEGER,
                    departmentId INTEGER REFERENCES ${PostgresDepartmentDataSource.table} (${PostgresDepartmentDataSource.departmentId}) 
                            ON DELETE CASCADE,
                    color VARCHAR(100),
                    technicalState VARCHAR(200),
                    rentPrice FLOAT NOT NULL
                )
            """
        private const val insertTransport = "INSERT INTO transport (type, mark, manufactureDate, seats, departmentId, color, technicalState, rentPrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        private const val updateTransport = "UPDATE transport SET type = ?, mark = ?, manufactureDate = ?, seats = ?, departmentId = ?, color = ?, technicalState = ?, rentPrice = ? WHERE transportId = ?"
        private const val getTransportById = "SELECT * FROM transport WHERE transportId = ?"
        private const val getAllTransport =
            """
                SELECT t.transportid, t.type AS ttype, t.mark, t.manufacturedate, t.seats, t.departmentid, t.color, t.technicalstate, t.rentPrice,
                       d.studioid
                FROM transport t
                LEFT JOIN department d ON t.departmentid = d.departmentid
                WHERE d.studioid = ?
            """
//        private const val getAllTransportFiltered = "SELECT * FROM transport WHERE departmentId = ? AND (type ILIKE ? OR mark ILIKE ? OR manufactureDate ILIKE ? OR seats ILIKE ? OR color ILIKE ? OR technicalState ILIKE ?))"
        private const val deleteTransport = "DELETE FROM transport WHERE transportId = ?"
    }

    init {
        connection.createStatement()
            .executeUpdate(createTableTransport)
    }

    override suspend fun insertUpdateTransport(transport: Transport): Int? = withContext(Dispatchers.IO) {
        val statement = if (transport.transportId != null) {
            connection.prepareStatement(updateTransport).apply {
                setInt(1, transport.type.fromTransportType())
                setString(2, transport.mark)
                setDate(3, Date(transport.manufactureDate))
                setInt(4, transport.seats)
                setInt(5, transport.departmentId)
                setString(6, transport.color)
                setString(7, transport.technicalState)
                setFloat(8, transport.rentPrice)
                setInt(9, transport.transportId)
            }
        } else {
            connection.prepareStatement(insertTransport, Statement.RETURN_GENERATED_KEYS).apply {
                setInt(1, transport.type.fromTransportType())
                setString(2, transport.mark)
                setDate(3, Date(transport.manufactureDate))
                setInt(4, transport.seats)
                setInt(5, transport.departmentId)
                setString(6, transport.color)
                setString(7, transport.technicalState)
                setFloat(8, transport.rentPrice)
            }
        }
        statement.executeUpdate()

        return@withContext if (transport.transportId != null) {
            transport.transportId
        }
        else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        }
        else null
    }

    override suspend fun getTransportById(id: Int): Transport? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getTransportById)
        statement.setInt(1, id)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Transport (
                transportId = result.getInt("transportId"),
                type = result.getInt("type").toTransportType(),
                mark = result.getString("mark"),
                manufactureDate = result.getDate("manufactureDate").time,
                seats = result.getInt("seats"),
                departmentId = result.getInt("departmentId"),
                color = result.getString("color"),
                technicalState = result.getString("technicalState"),
                rentPrice = result.getFloat("rentPrice")
            )
        } else null
    }

    override suspend fun getAllTransport(
        studioId: Int,
        search: String?,
        type: TransportType?,
        manufactureDateFrom: Long?,
        manufactureDateTo: Long?,
        seatsFrom: Int?,
        seatsTo: Int?
    ): List<Transport> = withContext(Dispatchers.IO) {

        val filterParams = if(search?.isNotEmpty() == true
            || type != null
            || manufactureDateFrom != null
            || manufactureDateTo != null
            || seatsFrom != null
            || seatsTo != null
            ) {
            val formater = SimpleDateFormat("YYYY-MM-dd")
                StringBuilder().apply {
                    search?.let {
                        append(" AND (mark ILIKE '%$it%' OR color ILIKE '%$it%' OR technicalState ILIKE '%$it%') ")
                    }
                    type?.let {
                        append(" AND (t.type = ${it.fromTransportType()}) ")
                    }
                    when {
                        manufactureDateFrom != null && manufactureDateTo != null -> {
                            append(" AND ( manufacturedate BETWEEN '${formater.format(manufactureDateFrom)}' AND '${formater.format(manufactureDateTo)}' )")
                        }
                        manufactureDateFrom != null && manufactureDateTo == null -> {
                            append(" AND ( manufacturedate >= '${formater.format(manufactureDateFrom)}' ) ")
                        }
                        manufactureDateFrom == null && manufactureDateTo != null -> {
                            append(" AND ( manufacturedate <= '${formater.format(manufactureDateTo)}' )")
                        }
                    }
                    when {
                        seatsFrom != null && seatsTo != null -> {
                            append(" AND ( seats BETWEEN $seatsFrom AND $seatsTo )")
                        }
                        seatsFrom != null && seatsTo == null -> {
                            append(" AND ( seats >= $seatsFrom) ")
                        }
                        seatsFrom == null && seatsTo != null -> {
                            append(" AND ( seats <= $seatsTo )")
                        }
                    }
                }

        } else null

        val sqlString = filterParams?.let { "$getAllTransport $it" } ?: getAllTransport


        val statement = connection.prepareStatement(sqlString)
        statement.setInt(1, studioId)

        val result = statement.executeQuery()
        return@withContext mutableListOf<Transport>().apply {
            while (result.next()) {
                add (
                    Transport(
                        transportId = result.getInt("transportId"),
                        type = result.getInt("ttype").toTransportType(),
                        mark = result.getString("mark"),
                        manufactureDate = result.getDate("manufactureDate").time,
                        seats = result.getInt("seats"),
                        departmentId = result.getInt("departmentId"),
                        color = result.getString("color"),
                        technicalState = result.getString("technicalState"),
                        rentPrice = result.getFloat("rentPrice")
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