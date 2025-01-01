package ua.rikutou.studiobackend.data.statistic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.util.PSQLException
import ua.rikutou.studiobackend.data.actor.Actor
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource.Companion.actorId
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource.Companion.name
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource.Companion.nickName
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource.Companion.role
import ua.rikutou.studiobackend.data.actor.PostgresActorDataSource.Companion.studioId
import ua.rikutou.studiobackend.data.equipment.Equipment
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion.comment
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion.eId
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion.rentPrice
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion.sId
import ua.rikutou.studiobackend.data.equipment.PostgresEquipmentDataSource.Companion.type
import ua.rikutou.studiobackend.data.equipment.toEquipmentType
import ua.rikutou.studiobackend.data.location.Location
import ua.rikutou.studiobackend.data.transport.Transport
import ua.rikutou.studiobackend.data.transport.toTransportType
import java.sql.Connection
import java.sql.Date
import java.util.Calendar

class PostgresStatisticDataSource(val connection: Connection) : StatisticDataSource {
    companion object {
        private const val minAvgMaxLocation = """
            select min(c) as mi, avg(c) as a, max(c) as ma  FROM (
            select count(*) as c
            from document
            left join documenttolocation on document.documentid = documenttolocation.documentid
            left join location on documenttolocation.locationid = location.locationid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid)
        """
        private const val minAvgMaxTransport = """
            select min(c) as mi, avg(c) as a, max(c) as ma  FROM (
            select count(*) as c
            from document
            left join documenttotransport on document.documentid = documenttotransport.documentid
            left join transport on documenttotransport.transportid = transport.transportid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid)
        """
        private const val minAvgMaxEquipment = """
            select min(c) as mi, avg(c) as a, max(c) as ma  FROM (
            select count(*) as c
            from document
            left join documenttoequipment on document.documentid = documenttoequipment.documentid
            left join equipment on documenttoequipment.equipmentid = equipment.equipmentid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid)
        """

        private const val sumOfLocatinsByDocuments = """
            select sum(s) as locationPrice FROM (
            select sum(location.rentprice*days) as s
            from document
            left join documenttolocation on document.documentid = documenttolocation.documentid
            left join location on documenttolocation.locationid = location.locationid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid)
        """

        private const val sumOfTransportByDocument = """
            select sum(s) as transportPrice FROM (
            select sum(transport.rentprice*days) as s
            from document
            left join documenttotransport on document.documentid = documenttotransport.documentid
            left join transport on documenttotransport.transportid = transport.transportid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid);
        """

        private const val sumOfEquipmentByDocument = """
            select sum(s) as equipmentPrice FROM (
            select sum(equipment.rentprice*days) as s
            from document
            left join documenttoequipment on document.documentid = documenttoequipment.documentid
            left join equipment on documenttoequipment.equipmentid = equipment.equipmentid
            where document.studioid = ? AND document.datestart between ? AND ?
            GROUP BY document.documentid)
        """
        private const val quantityOfDocuments = "select count(*) as total from document WHERE studioId = ?"

        private const val mostPopularLocations = """
            select * from location where locationid in (
            select location.locationid
            from document
            left join documenttolocation on document.documentid = documenttolocation.documentid
            left join location on documenttolocation.locationid = location.locationid
            where document.studioid = ? AND document.datestart between ? AND ?
            group by location.locationid
            order by count(location.locationid) desc limit 1)
        """

        private const val mostPopularTransport = """
            select * from transport where transportid in (
            select transport.transportid
            from document
            left join documenttotransport on document.documentid = documenttotransport.documentid
            left join transport on documenttotransport.transportid = transport.transportid
            where document.studioid = ? AND document.datestart between ? AND ?
            group by transport.transportid
            order by count(transport.transportid) desc limit 1)
        """

        private const val mostPopularEquipment = """
            select * from equipment where equipmentid in (
            select equipment.equipmentid
            from document
            left join documenttoequipment on document.documentid = documenttoequipment.documentid
            left join equipment on documenttoequipment.equipmentid = equipment.equipmentid
            where document.studioid = ? AND document.datestart between ? AND ?
            group by equipment.equipmentid
            order by count(equipment.equipmentid) desc limit 1)
        """

        private const val mostPopularActor = """
            select actor.actorid, name, nickname, actor.role, studioid
            from actor
                left join actor_film on actor.actorid = actor_film.actorid
                left join film on actor_film.filmid = film.filmid
            where actor.actorid in (
                select actor.actorid
                from actor
                left join actor_film on actor.actorid = actor_film.actorid
                left join film on actor_film.filmid = film.filmid
                where film.date between ? AND ?
            group by actor.actorid
            order by count(film.filmid) desc) limit 1
        """
    }

    override suspend fun getStatistics(studioId: Int): Statistic = withContext(Dispatchers.IO){
        var statistic = Statistic()
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val lastDayOfMonth = calender.get(Calendar.DAY_OF_MONTH)
        calender.set(year, month, 1,0,0,0)
        val startOfMonth = calender.time.time
        calender.set(year, month, lastDayOfMonth,23,59,59)
        val endOfMonth = calender.time.time
        calender.set(year,0,1,0,0,0)
        val startOfYear = calender.time.time
        calender.set(year,11,31,23,59,59)
        val endOfYear = calender.time.time
        
        val minAvgMaxLocationStatement = connection.prepareStatement(minAvgMaxLocation)
        minAvgMaxLocationStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val resultLocation = minAvgMaxLocationStatement.executeQuery()
        resultLocation.next()
        statistic = statistic.copy(
            location = MinAvgMax(
                min = resultLocation.getInt("mi"),
                avg = resultLocation.getInt("a"),
                max = resultLocation.getInt("ma")
            )
        )
        
        val minAvgMaxtransportStatement = connection.prepareStatement(minAvgMaxTransport)
        minAvgMaxtransportStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val resultTransport = minAvgMaxtransportStatement.executeQuery()
        resultTransport.next()
        statistic = statistic.copy(
            transport = MinAvgMax(
                min = resultTransport.getInt("mi"),
                avg = resultTransport.getInt("a"),
                max = resultTransport.getInt("ma")
            )
        )

        val minAvgMaxequipmentStatement = connection.prepareStatement(minAvgMaxEquipment)
        minAvgMaxequipmentStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val resultEquipment = minAvgMaxequipmentStatement.executeQuery()
        resultEquipment.next()
        statistic = statistic.copy(
            equipment = MinAvgMax(
                min = resultEquipment.getInt("mi"),
                avg = resultEquipment.getInt("a"),
                max = resultEquipment.getInt("ma")
            )
        )

        val sumOfLocationsStatement = connection.prepareStatement(sumOfLocatinsByDocuments)
        sumOfLocationsStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val incomeByLocation = sumOfLocationsStatement.executeQuery()
        incomeByLocation.next()
        statistic = statistic.copy(
            incomeStructure = IncomeStructure(
                byLocation = incomeByLocation.getFloat("locationPrice") 
            )
        )

        val sumOfTransportsStatement = connection.prepareStatement(sumOfTransportByDocument)
        sumOfTransportsStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val incomeByTransport = sumOfTransportsStatement.executeQuery()
        incomeByTransport.next()
        statistic = statistic.copy(
            incomeStructure = statistic.incomeStructure?.copy(
                byTransport = incomeByTransport.getFloat("transportPrice")
            )
        )

        val sumOfEquipmentsStatement = connection.prepareStatement(sumOfEquipmentByDocument)
        sumOfEquipmentsStatement.apply {
            setInt(1,studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val incomeByEquipment = sumOfEquipmentsStatement.executeQuery()
        incomeByEquipment.next()
        statistic = statistic.copy(
            incomeStructure = statistic.incomeStructure?.copy(
                byEquipment = incomeByEquipment.getFloat("equipmentPrice")
            )
        )
        
        statistic = statistic.copy(
            incomeStructure = statistic.incomeStructure?.copy(
                total = (statistic.incomeStructure?.byLocation ?: 0F) + (statistic.incomeStructure?.byTransport ?: 0F) + (statistic.incomeStructure?.byEquipment ?: 0F)
            )
        )
        
        val quantityStatement = connection.prepareStatement(quantityOfDocuments)
        quantityStatement.setInt(1, studioId)
        val quantity = quantityStatement.executeQuery()
        quantity.next()
        statistic = statistic.copy(
            documentsTotal = quantity.getInt("total")
        )
        
        val popularLocationsStatement = connection.prepareStatement(mostPopularLocations)
        popularLocationsStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val popularLocationList = popularLocationsStatement.executeQuery()
        statistic = statistic.copy(
            mostPopularLocations = mutableListOf<Location>().apply { 
                while (popularLocationList.next()) {
                    add(
                        Location(
                            locationId = popularLocationList.getInt("locationId"),
                            name = popularLocationList.getString("name"),
                            address = popularLocationList.getString("address"),
                            width = popularLocationList.getFloat("width"),
                            length = popularLocationList.getFloat("length"),
                            height = popularLocationList.getFloat("height"),
                            type = popularLocationList.getString("type"),
                            studioId = popularLocationList.getInt("studioId"),
                            rentPrice = popularLocationList.getFloat("rentPrice"),
                        )
                    )
                }
            }
        )
        
        val popularTransportsStatement = connection.prepareStatement(mostPopularTransport)
        popularTransportsStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val popularTransportList = popularTransportsStatement.executeQuery()
        statistic = statistic.copy(
            mostPopularTransport = mutableListOf<Transport>().apply {
                while (popularTransportList.next()) {
                    add(
                        Transport(
                            transportId = popularTransportList.getInt("transportId"),
                            type = popularTransportList.getInt("type").toTransportType(),
                            mark = popularTransportList.getString("mark"),
                            manufactureDate = popularTransportList.getDate("manufactureDate").time,
                            seats = popularTransportList.getInt("seats"),
                            departmentId = popularTransportList.getInt("departmentId"),
                            color = popularTransportList.getString("color"),
                            technicalState = popularTransportList.getString("technicalState"),
                            rentPrice = popularTransportList.getFloat("rentPrice")
                        )
                    )
                }
            }
        )

        val popularEquipmentsStatement = connection.prepareStatement(mostPopularEquipment)
        popularEquipmentsStatement.apply {
            setInt(1, studioId)
            setDate(2, Date(startOfMonth))
            setDate(3,Date(endOfMonth))
        }
        val popularEquipmentList = popularEquipmentsStatement.executeQuery()
        statistic = statistic.copy(
            mostPopularEquipment = mutableListOf<Equipment>().apply {
                while (popularEquipmentList.next()) {
                    add(
                        Equipment(
                            equipmentId = popularEquipmentList.getInt(eId),
                            name = popularEquipmentList.getString(PostgresEquipmentDataSource.name),
                            type = popularEquipmentList.getInt(type).toEquipmentType(),
                            comment = popularEquipmentList.getString(comment),
                            rentPrice = popularEquipmentList.getFloat(rentPrice),
                            studioId = popularEquipmentList.getInt(sId),
                        )
                    )
                }
            }
        )
        
        val popularActorStatement = connection.prepareStatement(mostPopularActor)
        popularActorStatement.apply {
            setDate(1, Date(startOfYear))
            setDate(2,Date(endOfYear))
        }
        val popularActor = popularActorStatement.executeQuery()
        val success = popularActor.next()
        if(success) {
            statistic = statistic.copy(
                mostPopularActor = Actor(
                    actorId = popularActor.getInt(actorId),
                    name = popularActor.getString(name),
                    nickName = popularActor.getString(nickName),
                    role = popularActor.getString(role),
                    studioId = popularActor.getInt(studioId)
                )
            )
        }
        return@withContext statistic
    }
}