package ua.rikutou.studiobackend.data.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.document.request.DocumentRequest
import java.sql.Connection
import java.sql.Date
import java.sql.Statement
import java.util.*

class PostgresDataSourceDocument(private val connection: Connection) : DocumentDateSource {
    companion object {
        private const val createTableDocument = """
            CREATE TABLE IF NOT EXISTS document (
                documentId SERIAL PRIMARY KEY, 
                dateStart DATE, 
                dateEnd DATE,
                days INTEGER, 
                studioId INTEGER REFERENCES studio (studioId) ON DELETE CASCADE 
            )
        """
        private const val createTableDocumentToLocation =
            """
                CREATE TABLE IF NOT EXISTS documentToLocation (
                documentId INTEGER
                    REFERENCES document (documentId),
                locationId INTEGER
                    REFERENCES location (locationId)
                )
            """
        private const val createTableDocumentToTransport =
            """
                CREATE TABLE IF NOT EXISTS documentToTransport (
                documentId INTEGER
                    REFERENCES document (documentId),
                transportId INTEGER
                    REFERENCES transport (transportId)
                )
            """
        private const val createTableDocumentToEquipment =
            """
                CREATE TABLE IF NOT EXISTS documentToEquipment (
                documentId INTEGER
                    REFERENCES document (documentId),
                EquipmentId INTEGER
                    REFERENCES equipment (equipmentId)
                )
            """

        private const val insertDocument = """
            INSERT INTO document (dateStart, dateEnd, days)
            VALUES (?, ?, ?)
        """

        private const val insertDocumentToLocation = """
            INSERT INTO documentToLocation (documentId, locationId) VALUES (?, ?)
        """
        private const val insertDocumentToTransport = """
            INSERT INTO documentToTransport (documentId, transportId) VALUES (?, ?)
        """
        private const val insertDocumentToEquipment = """
            INSERT INTO documentToEquipment (documentId, equipmentId) VALUES (?, ?)
        """
    }
    init {
        connection
            .createStatement()
            .executeUpdate(createTableDocument)
        connection
            .createStatement()
            .executeUpdate(createTableDocumentToLocation)
        connection
            .createStatement()
            .executeUpdate(createTableDocumentToTransport)
        connection
            .createStatement()
            .executeUpdate(createTableDocumentToEquipment)
    }

    override suspend fun insertDocument(
        document: Document,
        locations: List<Int>,
        transport: List<Int>,
        equipment: List<Int>
    ): Int? = withContext(Dispatchers.IO) {
        val statementLocation = connection.prepareStatement(insertDocumentToLocation)
        val statementTransport = connection.prepareStatement(insertDocumentToTransport)
        val statementEquipment = connection.prepareStatement(insertDocumentToEquipment)
        val statementDocument = connection.prepareStatement(insertDocument, Statement.RETURN_GENERATED_KEYS)
        statementDocument.apply {
            val dateStart = Date(document.dateStart)
            val calendar = Calendar.getInstance()
            calendar.time = dateStart
            calendar.add(Calendar.DAY_OF_MONTH, document.days)
            val dateEnd = calendar.time
            setDate(1, Date(document.dateStart))
            setDate(2, Date(dateEnd.time))
            setInt(3, document.days)
        }.executeUpdate()

        val id = if(statementDocument.generatedKeys.next()) {
            statementDocument.generatedKeys.getInt(1)
        } else null

        id?.let {
            locations.forEach { location ->
                statementLocation.apply {
                    setInt(1,id)
                    setInt(2,location)
                }.executeUpdate()
            }
            transport.forEach { transport ->
                statementTransport.apply {
                    setInt(1,id)
                    setInt(2,transport)
                }.executeUpdate()
            }
            equipment.forEach { equipment ->
                statementEquipment.apply {
                    setInt(1,id)
                    setInt(2,equipment)
                }.executeUpdate()
            }
        }

        return@withContext id
    }
}