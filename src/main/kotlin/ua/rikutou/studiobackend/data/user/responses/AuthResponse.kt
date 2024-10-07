package ua.rikutou.studiobackend.data.user.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
