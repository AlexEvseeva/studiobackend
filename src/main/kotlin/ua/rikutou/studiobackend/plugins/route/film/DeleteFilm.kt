package ua.rikutou.studiobackend.plugins.route.film

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.film.FilmDataSource

fun Route.deleteFilm() {
    authenticate {
        delete("film/{filmId}") {
            val filmDataSource by application.inject<FilmDataSource>()

            val filmId = call.parameters["filmId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Film Id not found"
                    )
                )
                return@delete
            }

            filmDataSource.deleteFilm(filmId)
            call.respond(
                status = HttpStatusCode.NoContent,
                message = "Film deleted")
        }
    }
}