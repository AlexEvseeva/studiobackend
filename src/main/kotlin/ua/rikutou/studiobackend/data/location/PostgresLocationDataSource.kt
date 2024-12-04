package ua.rikutou.studiobackend.data.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import java.sql.Connection
import java.sql.Statement

class PostgresLocationDataSource(private val connection: Connection) : LocationDataSource {
    companion object {
        private const val createTableLocation =
            """
                CREATE TABLE IF NOT EXISTS location (
                    locationId SERIAL PRIMARY KEY,
                    name VARCHAR(100), 
                    address VARCHAR(200),
                    width FLOAT,
                    length FLOAT,
                    height FLOAT,
                    type VARCHAR(50) NOT NULL,
                    studioId INTEGER 
                        REFERENCES ${PostgresStudioDataSource.table} (${PostgresStudioDataSource.studioId}) 
                            ON DELETE CASCADE,
                    rentPrice FLOAT
                )
            """
        private const val insertLocation = "INSERT INTO location (name, address, width, length, height, type, studioId,rentPrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        private const val getLocationByName = "SELECT * FROM location WHERE name = ?"
        private const val getLocationById = "SELECT * FROM location WHERE locationId = ?"
        private const val updateLocationStudioId = "UPDATE location SET studioId = ? WHERE locationId = ?"
        private const val getAllLocations = "SELECT * FROM location WHERE studioId = ?"
        private const val updateLocation = "UPDATE location SET name = ?, address = ?, width = ?, length = ?, height = ?, type = ?,rentPrice = ? WHERE locationId = ?"
        private const val deleteLocation = "DELETE FROM location WHERE locationId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableLocation)
    }
    override suspend fun insertLocation(location: Location): Int? = withContext(Dispatchers.IO) {
        getLocationByName(name = location.name)?.let {
            return@withContext null
        }
        val statement = connection.prepareStatement(insertLocation, Statement.RETURN_GENERATED_KEYS)
        statement.apply {
            setString(1, location.name)
            setString(2, location.address)
            setFloat(3, location.width)
            setFloat(4, location.length)
            setFloat(5, location.height)
            setString(6, location.type)
            setInt(7, location.studioId)
            setFloat(8, location.rentPrice)
        }.executeUpdate()

        return@withContext if(statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getLocationByName(name: String): Location? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getLocationByName)
        statement.setString(1, name)
        val result = statement.executeQuery()

        return@withContext if(result.next()) {
            Location(
                locationId = result.getInt("locationId"),
                name = result.getString("name"),
                address = result.getString("address"),
                width = result.getFloat("width"),
                length = result.getFloat("length"),
                height = result.getFloat("height"),
                type = result.getString("type"),
                studioId = result.getInt("studioId"),
                rentPrice = result.getFloat("rentPrice"),
            )
        } else null
    }

    override suspend fun getLocationById(locationId: Int): Location? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getLocationById)
        statement.setInt(1, locationId)
        val result = statement.executeQuery()

        return@withContext if(result.next()) {
            Location(
                locationId = result.getInt("locationId"),
                name = result.getString("name"),
                address = result.getString("address"),
                width = result.getFloat("width"),
                length = result.getFloat("length"),
                height = result.getFloat("height"),
                type = result.getString("type"),
                studioId = result.getInt("studioId"),
                rentPrice = result.getFloat("rentPrice"),
            )
        } else null
    }

    override suspend fun updateLocation(locationId: Int?, studioId: Int?, location: Location?) = withContext(Dispatchers.IO) {

        if (locationId != null && studioId != null) {
            val statement = connection.prepareStatement(updateLocationStudioId)
            statement.apply {
                setInt(1, studioId)
                setInt(2, locationId)
            }.executeUpdate()
            return@withContext

        } else if (location != null) {
            val statement = connection.prepareStatement(updateLocation)
            statement.apply {
                setString(1, location.name)
                setString(2, location.address)
                setFloat(3, location.width)
                setFloat(4, location.length)
                setFloat(5, location.height)
                setString(6, location.type)
                setFloat(8, location.rentPrice)
                setInt(7, location.locationId ?: -1)
            }
            val count = statement.executeUpdate()
            return@withContext
        }
    }

    override suspend fun getAllLocations(
        studioId: Int,
        search: String?,
        type: String?,
        widthFrom: Int?,
        widthTo: Int?,
        lengthFrom: Int?,
        lengthTo: Int?,
        heightFrom: Int?,
        heightTo: Int?
    ): List<Location> = withContext(Dispatchers.IO) {

        val filterParams =if(search?.isNotEmpty() == true
            || type?.isNotEmpty() == true
            || widthFrom != null
            || widthTo != null
            || lengthFrom != null
            || lengthTo != null
            || heightFrom != null
            || heightTo != null ) {
            StringBuilder().apply {
            search?.let {
                append(" AND (name ILIKE '%$it%' OR address ILIKE '%$it%')")
            }
            when {
                widthFrom != null && widthTo != null -> append(" AND width BETWEEN $widthFrom AND $widthTo")
                widthFrom != null && widthTo == null -> append(" AND width >= $widthFrom")
                widthFrom == null && widthTo != null -> append(" AND width <= $widthTo")
            }
            when {
                lengthFrom != null && lengthTo != null -> append(" AND length BETWEEN $lengthFrom AND $lengthTo")
                lengthFrom != null && lengthTo == null -> append(" AND length >= $lengthFrom")
                lengthFrom == null && lengthTo != null -> append(" AND length <= $lengthTo")
            }
            when {
                heightFrom != null && heightTo != null -> append(" AND height BETWEEN $heightFrom AND $heightTo")
                heightFrom != null && heightTo == null -> append(" AND height >= $heightFrom")
                heightFrom == null && heightTo != null -> append(" AND height <= $heightTo")
            }
        }.toString()
            } else null

        val sqlString = filterParams?.let { "$getAllLocations $it" } ?: getAllLocations

        val statement = connection.prepareStatement(sqlString)

        statement.setInt(1, studioId)
        val result = statement.executeQuery()
        return@withContext mutableListOf<Location>().apply {
            while(result.next()) {
                add(
                    Location(
                        locationId = result.getInt("locationId"),
                        name = result.getString("name"),
                        address = result.getString("address"),
                        width = result.getFloat("width"),
                        length = result.getFloat("length"),
                        height = result.getFloat("height"),
                        type = result.getString("type"),
                        studioId = result.getInt("studioId"),
                        rentPrice = result.getFloat("rentPrice"),
                    )
                )
            }
        }
    }

    override suspend fun deleteById(locationId: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteLocation)
        statement.setInt(1, locationId)
        statement.execute()
    }
}