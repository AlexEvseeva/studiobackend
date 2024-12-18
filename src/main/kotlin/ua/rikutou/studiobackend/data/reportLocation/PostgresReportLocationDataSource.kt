package ua.rikutou.studiobackend.data.reportLocation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.location.Location
import java.sql.Connection
import java.sql.Date

class PostgresReportLocationDataSource(val connection: Connection) : ReportLocationDataSource {
    companion object {
        private const val getFreeLocations = """
            select distinct location.locationid,
                name,
                address,
                width,
                length,
                height,
                type,
                location.studioid,
                rentprice
from location
left join public.documenttolocation dl on location.locationid = dl.locationid
left join public.document d on d.documentid = dl.documentid
where (dl.documentid is null or d.dateend < ?)
and location.deleted is null
and location.studioid = ?;
        """

        private const val getFreeLocationsCount = """
            select count(loc) from (
                select distinct location.name as loc from location
                left join public.documenttolocation dl on location.locationid = dl.locationid
                left join public.document d on d.documentid = dl.documentid
                where (dl.documentid is null or d.dateend < ?)
                and location.deleted is null
                and location.studioid = ? )
        """

        private const val getLocationsCount = """
            select count(*) from location where location.deleted is null and studioId = ?
        """
    }

    override suspend fun getReportLocation(studioId: Int): ReportLocation = withContext(Dispatchers.IO) {
        var report = ReportLocation()

        val freelocationsStatemnt = connection.prepareStatement(getFreeLocations)
        freelocationsStatemnt.apply {
            setDate(1, Date(java.util.Date().time))
            setInt(2, studioId)
        }
        val freeLocations = freelocationsStatemnt.executeQuery()
        report = report.copy(
            locations = mutableListOf<Location>().apply {
                while (freeLocations.next()) {
                    add(
                        Location(
                            locationId = freeLocations.getInt("locationId"),
                            name = freeLocations.getString("name"),
                            address = freeLocations.getString("address"),
                            width = freeLocations.getFloat("width"),
                            length = freeLocations.getFloat("length"),
                            height = freeLocations.getFloat("height"),
                            type = freeLocations.getString("type"),
                            studioId = freeLocations.getInt("studioId"),
                            rentPrice = freeLocations.getFloat("rentPrice"),
                        )
                    )
                }
            }
        )

        val freelocationCountStatement = connection.prepareStatement(getFreeLocationsCount)
        freelocationCountStatement.apply {
            setDate(1, Date(java.util.Date().time))
            setInt(2, studioId)
        }
        val freelocationCount = freelocationCountStatement.executeQuery()
        if (freelocationCount.next()) {
            report = report.copy(
                locationsCount = freelocationCount.getInt(1)
            )
        }

        val locationCountStatement = connection.prepareStatement(getLocationsCount)
        locationCountStatement.apply {
            setInt(1, studioId)
        }
        val locationCount = locationCountStatement.executeQuery()
        if (locationCount.next()) {
            report = report.copy(
                locationsCountTotal = locationCount.getInt(1)
            )
        }

        return@withContext report
    }
}