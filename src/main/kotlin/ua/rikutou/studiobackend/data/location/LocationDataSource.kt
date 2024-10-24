package ua.rikutou.studiobackend.data.location

interface LocationDataSource {
    suspend fun insertLocation(location: Location): Int?
    suspend fun getLocationByName(name: String): Location?
    suspend fun getLocationById(locationId: Int): Location?
    suspend fun updateLocation(locationId: Int, studioId: Int)
    suspend fun getAllLocations(studioId: Int): List<Location>
}