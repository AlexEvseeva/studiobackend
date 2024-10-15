package ua.rikutou.studiobackend.data.studio

interface StudioDataSource {
    suspend fun insertStudio(studio: Studio): Int?
    suspend fun getStudioById(studioId: Int): Studio?
    suspend fun getStudioByName(name: String): Studio?
}