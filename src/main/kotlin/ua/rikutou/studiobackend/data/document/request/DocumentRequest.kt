package ua.rikutou.studiobackend.data.document.request

import kotlinx.serialization.Serializable

@Serializable
data class DocumentRequest(
    val dateStart: Long,
    val days: Int,
    val locations: List<Int>,
    val transport: List<Int>,
    val equipment: List<Int>,
    val studioId: Int
)
