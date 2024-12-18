package ua.rikutou.studiobackend.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.plugins.route.actor.createUpdateActor
import ua.rikutou.studiobackend.plugins.route.actor.deleteActor
import ua.rikutou.studiobackend.plugins.route.actor.getActorById
import ua.rikutou.studiobackend.plugins.route.actor.getAllActors
import ua.rikutou.studiobackend.plugins.route.auth.me
import ua.rikutou.studiobackend.plugins.route.auth.signIn
import ua.rikutou.studiobackend.plugins.route.auth.signUp
import ua.rikutou.studiobackend.plugins.route.department.createUpdateDepartment
import ua.rikutou.studiobackend.plugins.route.department.deleteDepartment
import ua.rikutou.studiobackend.plugins.route.department.getAllDepartments
import ua.rikutou.studiobackend.plugins.route.department.getDepartmentById
import ua.rikutou.studiobackend.plugins.route.document.createDocument
import ua.rikutou.studiobackend.plugins.route.equipment.createUpdateEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.deleteEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.getAllEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.getEquipmentById
import ua.rikutou.studiobackend.plugins.route.execute.executeQuery
import ua.rikutou.studiobackend.plugins.route.film.createUpdateFilm
import ua.rikutou.studiobackend.plugins.route.film.deleteFilm
import ua.rikutou.studiobackend.plugins.route.film.getFilmById
import ua.rikutou.studiobackend.plugins.route.location.createUpdateLocation
import ua.rikutou.studiobackend.plugins.route.location.deleteLocation
import ua.rikutou.studiobackend.plugins.route.location.getLocationById
import ua.rikutou.studiobackend.plugins.route.location.getLocations
import ua.rikutou.studiobackend.plugins.route.reportLocation.getReportLocation
import ua.rikutou.studiobackend.plugins.route.section.createUpdateSection
import ua.rikutou.studiobackend.plugins.route.section.deleteSection
import ua.rikutou.studiobackend.plugins.route.section.getAllSections
import ua.rikutou.studiobackend.plugins.route.section.getSectionById
import ua.rikutou.studiobackend.plugins.route.statistic.getStatistic
import ua.rikutou.studiobackend.plugins.route.studio.createUpdateStudio
import ua.rikutou.studiobackend.plugins.route.studio.deleteStudio
import ua.rikutou.studiobackend.plugins.route.studio.getStudio
import ua.rikutou.studiobackend.plugins.route.transport.createUpdateTransport
import ua.rikutou.studiobackend.plugins.route.transport.deleteTransport
import ua.rikutou.studiobackend.plugins.route.transport.getAllTransport
import ua.rikutou.studiobackend.plugins.route.transport.getTransportById
import ua.rikutou.studiobackend.plugins.route.user.deleteUser
import ua.rikutou.studiobackend.plugins.route.user.getUsersByStudioIdAndCandidates
import ua.rikutou.studiobackend.plugins.route.user.updateUserStudio
import java.io.File

fun Application.configureRouting() {
    routing {
        staticFiles(
            remotePath = "/uploads",
            dir = File("uploads")
        )
        signIn()
        signUp()
        me()

        getStudio()
        createUpdateStudio()
        deleteStudio()

        getLocationById()
        createUpdateLocation()
        getLocations()
        deleteLocation()

        createUpdateEquipment()
        getAllEquipment()
        getEquipmentById()
        deleteEquipment()

        createUpdateDepartment()
        deleteDepartment()
        getAllDepartments()
        getDepartmentById()

        createUpdateSection()
        getSectionById()
        getAllSections()
        deleteSection()

        createUpdateTransport()
        getTransportById()
        getAllTransport()
        deleteTransport()

        createUpdateActor()
        getActorById()
        getAllActors()
        deleteActor()

        createUpdateFilm()
        getFilmById()
        deleteFilm()

        getUsersByStudioIdAndCandidates()
        deleteUser()
        updateUserStudio()

        createDocument()
        getStatistic()

        getReportLocation()

        executeQuery()
    }
}
