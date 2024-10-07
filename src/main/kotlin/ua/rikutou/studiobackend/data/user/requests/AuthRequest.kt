package ua.rikutou.studiobackend.data.user.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val name: String,
    val password: String
)
