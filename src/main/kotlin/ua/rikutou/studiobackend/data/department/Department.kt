package ua.rikutou.studiobackend.data.department

import kotlinx.serialization.Serializable

@Serializable
data class Department(
    val departmentId: Int? = null,
    val type: String,
    val workHours: String,
    val contactPerson: String,
    val studioId: Int,
)
