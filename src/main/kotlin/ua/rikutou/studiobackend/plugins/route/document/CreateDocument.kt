package ua.rikutou.studiobackend.plugins.route.document

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.document.Document
import ua.rikutou.studiobackend.data.document.DocumentDateSource
import ua.rikutou.studiobackend.data.document.request.DocumentRequest

fun Route.createDocument() {
    authenticate {
        post("document") {
            val documentDataSource by application.inject<DocumentDateSource>()

            val request = call.runCatching {
                this.receiveNullable<DocumentRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Document's fields not found"
                    )
                )
                return@post
            }

            val document = Document(
                dateStart = request.dateStart,
                days = request.days,
                studioId = request.studioId
            )
            val id = documentDataSource.insertDocument(
                document = document,
                locations = request.locations,
                transport = request.transport,
                equipment = request.equipment,
            )
            call.respond(
                status = HttpStatusCode.OK,
                message = document.copy(documentId = id)
            )
        }
    }
}