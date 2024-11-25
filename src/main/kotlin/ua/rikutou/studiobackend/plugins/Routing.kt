package ua.rikutou.studiobackend.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import ua.rikutou.studiobackend.plugins.route.auth.me
import ua.rikutou.studiobackend.plugins.route.auth.signIn
import ua.rikutou.studiobackend.plugins.route.auth.signUp
import ua.rikutou.studiobackend.plugins.route.department.createUpdateDepartment
import ua.rikutou.studiobackend.plugins.route.department.deleteDepartment
import ua.rikutou.studiobackend.plugins.route.department.getAllDepartments
import ua.rikutou.studiobackend.plugins.route.department.getDepartmentById
import ua.rikutou.studiobackend.plugins.route.equipment.createUpdateEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.deleteEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.getAllEquipment
import ua.rikutou.studiobackend.plugins.route.equipment.getEquipmentById
import ua.rikutou.studiobackend.plugins.route.location.createUpdateLocation
import ua.rikutou.studiobackend.plugins.route.location.deleteLocation
import ua.rikutou.studiobackend.plugins.route.location.getLocationById
import ua.rikutou.studiobackend.plugins.route.location.getLocations
import ua.rikutou.studiobackend.plugins.route.section.createUpdateSection
import ua.rikutou.studiobackend.plugins.route.section.deleteSection
import ua.rikutou.studiobackend.plugins.route.section.getAllSections
import ua.rikutou.studiobackend.plugins.route.section.getSectionById
import ua.rikutou.studiobackend.plugins.route.studio.createUpdateStudio
import ua.rikutou.studiobackend.plugins.route.studio.getStudio
import ua.rikutou.studiobackend.plugins.route.user.getUsersByStudioIdAndCandidates
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

        getUsersByStudioIdAndCandidates()
    }
}
