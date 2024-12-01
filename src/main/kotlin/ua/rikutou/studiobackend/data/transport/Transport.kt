package ua.rikutou.studiobackend.data.transport

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Transport(
    val transportId: Int? = null,
    val type: String,
    val mark: String,
    val manufactureDate: Long,
    val seats: Int,
    val departmentId: Int,
    val color: String,
    val technicalState: String,
)
