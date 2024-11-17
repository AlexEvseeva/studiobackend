package ua.rikutou.studiobackend.data.location

interface LocationDataSource {
    suspend fun insertLocation(location: Location): Int?
    suspend fun getLocationByName(name: String): Location?
    suspend fun getLocationById(locationId: Int): Location?
    suspend fun updateLocation(locationId: Int? = null, studioId: Int? = null, location: Location? = null)
    suspend fun getAllLocations(studioId: Int, search: String? = null): List<Location>
    suspend fun deleteById(locationId: Int)
}