package ua.rikutou.studiobackend.data.email

import kotlinx.serialization.Serializable

@Serializable
data class Email(
    val emailId: Int,
    val email: String,
)
