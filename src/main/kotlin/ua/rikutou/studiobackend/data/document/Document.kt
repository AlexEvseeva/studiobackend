package ua.rikutou.studiobackend.data.document

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val documentId: Int? = null,
    val dateStart: Long,
    val days: Int,
    val studioId: Int
)
