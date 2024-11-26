package ua.rikutou.studiobackend.data.transport

import kotlinx.serialization.Serializable

@Serializable
data class Transport(
    val transportId: Int? = null,
    val type: String,
    val mark: String,
    val manufactureDate: String,
    val seats: Int,
    val departmentId: Int,
    val color: String,
    val technicalState: String,
)
