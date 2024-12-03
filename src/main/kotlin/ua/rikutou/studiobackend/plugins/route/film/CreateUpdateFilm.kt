package ua.rikutou.studiobackend.plugins.route.film

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.film.Film
import ua.rikutou.studiobackend.data.film.FilmDataSource
import ua.rikutou.studiobackend.data.film.requests.FilmRequest

fun Route.createUpdateFilm() {
    authenticate {
        post("film") {
            val filmDataSource by application.inject<FilmDataSource>()

            val request = call.runCatching {
                this.receiveNullable<FilmRequest>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error (
                        code = HttpStatusCode.BadRequest.value,
                        message = "Film's fields not found"
                    )
                )
                return@post
            }

            val film = Film (
                filmId = request.filmId,
                title = request.title,
                genres = request.genres,
                director = request.director,
                writer = request.writer,
                date = request.date,
                budget = request.budget
            )
            val id = filmDataSource.insertUpdatedFilm(film = film)
            call.respond(
                status = HttpStatusCode.OK,
                message = film.copy(filmId = id)
            )
        }
    }
}