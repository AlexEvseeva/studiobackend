package ua.rikutou.studiobackend.data.equipment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import java.sql.Connection
import java.sql.Date
import java.sql.Statement

class PostgresEquipmentDataSource(private val connection: Connection) : EquipmentDataSource {
    companion object {
        const val table = "equipment"
        const val eId = "equipmentId"
        const val name = "name"
        const val type = "type"
        const val comment = "comment"
        const val rentPrice = "rentPrice"
        const val sId = "studioId"

        private const val createTableEquipment =
            """
                CREATE TABLE IF NOT EXISTS $table (
                    $eId SERIAL PRIMARY KEY,
                    $name VARCHAR(100),
                    $type INTEGER,
                    $comment VARCHAR(300),
                    $rentPrice FLOAT,
                    $sId INTEGER
                        REFERENCES ${PostgresStudioDataSource.table} (${PostgresStudioDataSource.studioId})
                        ON DELETE CASCADE
                )
            """
        private const val insertEquipment = "INSERT INTO $table ($name, $type, $comment, $rentPrice, $sId) VALUES (?, ?, ?, ?, ?)"
        private const val updateEquipment = "UPDATE $table SET $sId = ?, $name = ?, $type = ?, $comment = ?, $rentPrice = ? WHERE $eId = ?"
        private const val delterEquipment = "UPDATE $table SET deleted = 1 WHERE $eId = ?"
        private const val getAllEquipment = """
            SELECT * FROM $table 
            left join documenttoequipment de on equipment.equipmentid = de.equipmentid
            left join document d on de.documentid = d.documentid
            where d.dateend < ?
            or de.equipmentid is null
            and equipment.$sId = ?
            and equipment.deleted is null
            
            AND case
                when ? is not null THEN $name ILIKE ? OR $comment ILIKE ?
                else true
            END
            
            AND case
                when ? > 0 THEN $type = ?
                else true
            END
        """
        private const val getEquipmentById = "SELECT * FROM $table WHERE $eId = ?"
        private const val getEquipmentByName = "SELECT * FROM $table WHERE $name ILIKE ? LIMIT 1"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableEquipment)
    }

    override suspend fun insertUpdateEquipment(equipment: Equipment): Int? = withContext(Dispatchers.IO){
        val statement = if (equipment.equipmentId != null) {
            connection.prepareStatement(updateEquipment).apply {
                setInt(1, equipment.studioId)
                setString(2, equipment.name)
                setInt(3, equipment.type.toDb())
                setString(4, equipment.comment)
                setFloat(5, equipment.rentPrice)
                setInt(6, equipment.equipmentId)
            }
        } else {
            connection.prepareStatement(insertEquipment, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, equipment.name)
                setInt(2, equipment.type.toDb())
                setString(3, equipment.comment)
                setFloat(4, equipment.rentPrice)
                setInt(5, equipment.studioId)
            }
        }
        statement.executeUpdate()

        return@withContext if (equipment.equipmentId != null) {
            equipment.equipmentId
        } else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getEquipmentByName(name: String): Equipment? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getEquipmentByName)
        statement.setString(1, "%$name%")
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Equipment (
                equipmentId = result.getInt(eId),
                name = result.getString(Companion.name),
                type = result.getInt(type).toEquipmentType(),
                comment = result.getString(comment),
                rentPrice = result.getFloat(rentPrice),
                studioId = result.getInt(sId),
            )
        } else null
    }

    override suspend fun getEquipmentById(equipmentId: Int): Equipment? = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(getEquipmentById)
        statement.setInt(1, equipmentId)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Equipment(
                equipmentId = result.getInt(eId),
                name = result.getString(name),
                type = result.getInt(type).toEquipmentType(),
                comment = result.getString(comment),
                rentPrice = result.getFloat(rentPrice),
                studioId = result.getInt(sId),
            )
        } else null
    }

    override suspend fun getAllEquipment(studioId: Int, search: String?, equipmentType: EquipmentType?): List<Equipment> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getAllEquipment).apply {
            setDate(1, Date(java.util.Date().time))
            setInt(2, studioId)
            setString(3, search)
            setString(4,"%$search%")
            setString(5,"%$search%")
            setInt(6, equipmentType?.toDb() ?: -1)
            setInt(7, equipmentType?.toDb() ?: -1)
        }

        println("-------> $statement")

        val result = statement.executeQuery()
        return@withContext mutableListOf<Equipment>().apply {
            while (result.next()) {
                add(
                    Equipment(
                        equipmentId = result.getInt(eId),
                        name = result.getString(name),
                        type = result.getInt(type).toEquipmentType(),
                        comment = result.getString(comment),
                        rentPrice = result.getFloat(rentPrice),
                        studioId = result.getInt(PostgresEquipmentDataSource.sId),
                    )
                )
            }
        }
    }

    override suspend fun deleteById(equipmentId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(delterEquipment)
        statement.setInt(1, equipmentId)
        statement.executeUpdate()
    }
}