package ua.rikutou.studiobackend.data.transport

import ua.rikutou.studiobackend.data.department.Department

interface TransportDataSource {
    suspend fun insertUpdateTransport(transport: Transport): Int?
    suspend fun getTransportById(id: Int): Transport?
    suspend fun getAllTransport(departmentId: Int, search: String?): List<Transport>
    suspend fun deleteTransport(transportId: Int) : Unit
}