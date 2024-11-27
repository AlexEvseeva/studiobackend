package ua.rikutou.studiobackend.data.department

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.section.Section

@Serializable
data class Department(
    val departmentId: Int? = null,
    val type: String,
    val workHours: String,
    val contactPerson: String,
    val studioId: Int,
    val sections: List<Section>? = null,
)
