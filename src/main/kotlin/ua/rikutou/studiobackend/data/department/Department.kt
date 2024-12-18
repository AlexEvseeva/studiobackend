package ua.rikutou.studiobackend.data.department

import kotlinx.serialization.Serializable
import ua.rikutou.studiobackend.data.email.Email
import ua.rikutou.studiobackend.data.phone.Phone
import ua.rikutou.studiobackend.data.section.Section
import ua.rikutou.studiobackend.data.transport.Transport

@Serializable
data class Department(
    val departmentId: Int? = null,
    val type: String,
    val workHours: String,
    val contactPerson: String,
    val studioId: Int,
    val sections: List<Section>? = null,
    val transport: List<Transport>? = null,
    val phones: List<Phone>? = null,
    val emails: List<Email>? = null,
)
