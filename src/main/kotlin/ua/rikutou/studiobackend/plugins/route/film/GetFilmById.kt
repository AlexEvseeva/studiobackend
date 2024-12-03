package ua.rikutou.studiobackend.plugins.route.film

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.film.FilmDataSource

fun Route.getFilmById() {
    authenticate {
        get("/filmById") {
            val filmDataSource by application.inject<FilmDataSource>()

            val filmId = call.parameters["filmId"]?.toInt() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Film Id not found"
                    )
                )
                return@get
            }

            val film = filmDataSource.getFilmById(filmId) ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Error(
                        code = HttpStatusCode.NotFound.value,
                        message = "Film not found"
                    )
                )
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = film
            )
        }
    }
}