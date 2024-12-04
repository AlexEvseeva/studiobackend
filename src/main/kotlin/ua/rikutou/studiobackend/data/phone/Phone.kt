package ua.rikutou.studiobackend.data.phone

import kotlinx.serialization.Serializable

@Serializable
data class Phone(
    val phoneId: Int,
    val phoneNumber: String,
)
