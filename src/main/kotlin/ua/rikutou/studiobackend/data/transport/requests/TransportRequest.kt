package ua.rikutou.studiobackend.data.transport.requests

import kotlinx.serialization.Serializable

@Serializable
data class TransportRequest(
    val transportId: Int? = null,
    val type: Int,
    val mark: String,
    val manufactureDate: Long,
    val seats: Int,
    val departmentId: Int,
    val color: String,
    val technicalState: String,
)
