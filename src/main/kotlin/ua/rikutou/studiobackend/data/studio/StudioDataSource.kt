package ua.rikutou.studiobackend.data.studio

interface StudioDataSource {
    suspend fun insertStudio(studio: Studio): Int?
    suspend fun updateStudio(studio: Studio)
    suspend fun getStudioById(studioId: Int): Studio?
    suspend fun getStudioByName(name: String): Studio?
    suspend fun deleteStudioById(studioId: Int)
}