package ua.rikutou.studiobackend.data.execute.request

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val columns: List<String>,
    val queryResult: List<List<String>>
)
