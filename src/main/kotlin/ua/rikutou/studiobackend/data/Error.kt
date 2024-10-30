package ua.rikutou.studiobackend.data

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val code: Int,
    val message: String,
)
