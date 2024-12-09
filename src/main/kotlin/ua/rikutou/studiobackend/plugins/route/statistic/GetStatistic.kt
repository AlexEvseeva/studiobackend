package ua.rikutou.studiobackend.plugins.route.statistic

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.statistic.StatisticDataSource

fun Route.getStatistic() {
    authenticate {
        get("/statistic") {
            val statisticDataSource by application.inject<StatisticDataSource>()

            val studioId: Int = call.runCatching { parameters["studioId"]?.toInt() }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Studio id not found"
                    )
                )
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = statisticDataSource.getStatistics(studioId = studioId)
            )
        }
    }
 }