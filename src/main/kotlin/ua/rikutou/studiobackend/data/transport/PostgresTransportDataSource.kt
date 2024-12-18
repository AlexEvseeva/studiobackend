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
                left join documenttotransport dt on t.transportid = dt.transportid
                left join document d on d.documentid = dt.documentid
                LEFT JOIN department dep ON t.departmentid = dep.departmentid
                where d.dateend < ?
                or dt.transportid is null
                and dep.studioid = ?
                and t.deleted is null
                
                AND case
                    when ?::date is not null AND ?::date is not null then (manufacturedate between ?::date AND ?::date)
                    when ?::date is not null AND ?::date is null then (manufacturedate >= ?::date)
                    when ?::date is null and ?::date is not null then (manufacturedate <= ?::date)
                    else true
                end

                AND case
                    when ? is not null then mark ILIKE ? OR color ILIKE ? OR technicalstate ILIKE ?
                    else true
                END
                
                AND case
                    when ?::integer is not null AND ?::integer > 0 THEN t.type = ?::integer
                    else true
                END
                
                AND case
                    when ?::integer > 0 AND ?::integer > 0 THEN seats BETWEEN ?::integer AND ?::integer
                    when ?::integer > 0 AND ?::integer < 0 THEN  seats >= ?::integer
                    when ?::integer < 0 AND ?::integer > 0 THEN  seats <= ?::integer
                    else true
                END
            """
        private const val deleteTransport = "UPDATE transport SET deleted = 1 WHERE transportId = ?"
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

        val statement = connection.prepareStatement(getAllTransport).apply {

            setDate(1, Date(java.util.Date().time))
            setInt(2, studioId)

            setDate(3, manufactureDateFrom?.let { Date(it) })
            setDate(4, manufactureDateTo?.let { Date(it) })
            setDate(5, manufactureDateFrom?.let { Date(it) })
            setDate(6, manufactureDateTo?.let { Date(it) })

            setDate(7, manufactureDateFrom?.let { Date(it) })
            setDate(8, manufactureDateTo?.let { Date(it) })
            setDate(9, manufactureDateFrom?.let { Date(it) })

            setDate(10, manufactureDateFrom?.let { Date(it) })
            setDate(11, manufactureDateTo?.let { Date(it) })
            setDate(12, manufactureDateTo?.let { Date(it) })

            setString(13, search)
            setString(14, "%$search%")
            setString(15, "%$search%")
            setString(16, "%$search%")

            setInt(17, type?.fromTransportType() ?: -1)
            setInt(18, type?.fromTransportType() ?: -1)
            setInt(19, type?.fromTransportType() ?: -1)

            setInt(20, seatsFrom ?: -1)
            setInt(21, seatsTo ?: -1)
            setInt(22, seatsFrom ?: -1)
            setInt(23, seatsTo ?: -1)

            setInt(24, seatsFrom ?: -1)
            setInt(25, seatsTo ?: -1)
            setInt(26, seatsFrom ?: -1)

            setInt(27, seatsFrom ?: -1)
            setInt(28, seatsTo ?: -1)
            setInt(29, seatsTo ?: -1)
        }

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
        statement.executeUpdate()
    }
}