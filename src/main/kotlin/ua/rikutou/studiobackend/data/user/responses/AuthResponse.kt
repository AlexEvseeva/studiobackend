package ua.rikutou.studiobackend.data.user.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int?,
    val userName: String?,
    val studioId: Int? = null
)
