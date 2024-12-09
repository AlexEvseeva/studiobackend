package ua.rikutou.studiobackend.data.statistic

interface StatisticDataSource {
    suspend fun getStatistics(studioId: Int): Statistic
}