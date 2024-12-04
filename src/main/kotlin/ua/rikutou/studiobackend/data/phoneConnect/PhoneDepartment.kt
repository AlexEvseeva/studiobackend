package ua.rikutou.studiobackend.data.phoneConnect

import kotlinx.serialization.Serializable

@Serializable
data class PhoneDepartment(
    val phoneId: Int,
    val departmentId: Int,
)
