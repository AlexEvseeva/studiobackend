package ua.rikutou.studiobackend.data.document

import ua.rikutou.studiobackend.data.document.request.DocumentRequest

interface DocumentDateSource {
    suspend fun insertDocument(
        document: Document,
        locations: List<Int>,
        transport: List<Int>,
        equipment: List<Int>
    ): Int?
}