package ua.rikutou.studiobackend.data.section

import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val sectionId: Int? = null,
    val title: String,
    val address: String,
    val internalPhoneNumber: String,
    val departmentId: Int,
)
