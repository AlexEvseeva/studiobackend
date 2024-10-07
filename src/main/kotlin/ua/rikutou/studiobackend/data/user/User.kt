package ua.rikutou.studiobackend.data.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int? = null,
    val name: String,
    val password: String,
    val salt: String
)
