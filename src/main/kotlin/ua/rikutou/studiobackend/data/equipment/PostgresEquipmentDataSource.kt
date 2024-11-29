package ua.rikutou.studiobackend.data.equipment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresEquipmentDataSource(private val connection: Connection) : EquipmentDataSource {
    companion object {
        private const val table = "equipment"
        private const val eId = "equipmentId"
        private const val name = "name"
        private const val type = "type"
        private const val comment = "comment"
        private const val rentPrice = "rentPrice"
        private const val sId = "studioId"

        private const val createTableEquipment = "CREATE TABLE IF NOT EXISTS $table ($eId SERIAL PRIMARY KEY, $name VARCHAR(100), $type VARCHAR(100), $comment VARCHAR(300), $rentPrice FLOAT, $sId INTEGER NOT NULL)"
        private const val insertEquipment = "INSERT INTO $table ($name, $type, $comment, $rentPrice, $sId) VALUES (?, ?, ?, ?, ?)"
        private const val updateEquipment = "UPDATE $table SET $sId = ?, $name = ?, $type = ?, $comment = ?, $rentPrice = ? WHERE $eId = ?"
        private const val delterEquipment = "DELETE FROM $table WHERE $eId = ?"
        private const val getAllEquipment = "SELECT * FROM $table WHERE $sId = ?"
        private const val getEquipmentById = "SELECT * FROM $table WHERE $eId = ?"
        private const val getEquipmentByName = "SELECT * FROM $table WHERE $name ILIKE ? LIMIT 1"
        private const val getEquipmentsFiltered = "SELECT * FROM $table WHERE $sId = ? AND $name ILIKE ?"
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
                setString(3, equipment.type)
                setString(4, equipment.comment)
                setFloat(5, equipment.rentPrice)
                setInt(6, equipment.equipmentId)
            }
        } else {
            connection.prepareStatement(insertEquipment, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, equipment.name)
                setString(2, equipment.type)
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
                type = result.getString(type),
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
                type = result.getString(type),
                comment = result.getString(comment),
                rentPrice = result.getFloat(rentPrice),
                studioId = result.getInt(sId),
            )
        } else null
    }

    override suspend fun getAllEquipment(studioId: Int, search: String?): List<Equipment> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(search?.let {
            getEquipmentsFiltered
        } ?: getAllEquipment)
        statement.setInt(1, studioId)
        search?.let {
            statement.setString(2, "%$it%")
        }

        val result = statement.executeQuery()
        return@withContext mutableListOf<Equipment>().apply {
            while (result.next()) {
                add(
                    Equipment(
                        equipmentId = result.getInt(eId),
                        name = result.getString(name),
                        type = result.getString(type),
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
        statement.execute()
    }
}