package ua.rikutou.studiobackend.data.transport

import ua.rikutou.studiobackend.data.department.Department

interface TransportDataSource {
    suspend fun insertUpdateTransport(transport: Transport): Int?
    suspend fun getTransportById(id: Int): Transport?
    suspend fun getAllTransport(
        studioId: Int,
        search: String? = null,
        type: TransportType? = null,
        manufactureDateFrom: Long? = null,
        manufactureDateTo: Long? = null,
        seatsFrom: Int? = null,
        seatsTo: Int? = null,
    ): List<Transport>
    suspend fun deleteTransport(transportId: Int) : Unit
}