package ua.rikutou.studiobackend.data.execute.request

import kotlinx.serialization.Serializable

@Serializable
data class ExecuteQuery(
    val query: String,
)
