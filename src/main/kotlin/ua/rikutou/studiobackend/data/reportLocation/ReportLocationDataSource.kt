package ua.rikutou.studiobackend.data.reportLocation

interface ReportLocationDataSource {
    suspend fun getReportLocation(studioId: Int): ReportLocation
}