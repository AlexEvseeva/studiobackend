package ua.rikutou.studiobackend.plugins.route.execute

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.data.Error
import ua.rikutou.studiobackend.data.execute.ExecuteDataSource
import ua.rikutou.studiobackend.data.execute.request.ExecuteQuery
import ua.rikutou.studiobackend.data.execute.request.QueryResult

fun Route.executeQuery() {
    authenticate {
        post("execute") {

            val executeDataSource by application.inject<ExecuteDataSource>()

            val request = call.runCatching {
                this.receiveNullable<ExecuteQuery>()
            }.getOrNull() ?: run {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Error(
                        code = HttpStatusCode.BadRequest.value,
                        message = "Query request not found"
                    )
                )
                return@post
            }

            val queryResult = executeDataSource.execute(request.query)
            call.respond(
                status = HttpStatusCode.OK,
                message = QueryResult(
                    columns = queryResult.first(),
                    queryResult = queryResult.slice(1..queryResult.lastIndex)
                )
            )
        }
    }
}