package ua.rikutou.studiobackend.data.section.requests

import kotlinx.serialization.Serializable

@Serializable
data class SectionRequest(
    val sectionId: Int? = null,
    val title: String,
    val address: String,
    val internalPhoneNumber: String,
    val departmentId: Int,
)
