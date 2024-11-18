package ua.rikutou.studiobackend.data.department.requests

import kotlinx.serialization.Serializable

@Serializable
data class DepartmentRequest(
    val departmentId: Int? = null,
    val type: String,
    val workHours: String,
    val contactPerson: String,
    val studioId: Int,
)
