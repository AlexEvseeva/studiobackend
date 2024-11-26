package ua.rikutou.studiobackend.data.execute

interface ExecuteDataSource {
    suspend fun execute(query: String): List<List<String>>
}