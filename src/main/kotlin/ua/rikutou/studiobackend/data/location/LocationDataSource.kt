package ua.rikutou.studiobackend.data.location

interface LocationDataSource {
    suspend fun insertLocation(location: Location): Int?
    suspend fun getLocationByName(name: String): Location?
    suspend fun getLocationById(locationId: Int): Location?
    suspend fun updateLocation(location: Location)
    suspend fun getAllLocations(
        studioId: Int,
        search: String? = null,
        type: String? = null,
        widthFrom: Int? = null,
        widthTo: Int? = null,
        lengthFrom: Int? = null,
        lengthTo: Int? = null,
        heightFrom: Int? = null,
        heightTo: Int? = null,
    ): List<Location>
    suspend fun deleteById(locationId: Int)
    suspend fun updateLocationStdioId(locationId: Int, studioId: Int)
}