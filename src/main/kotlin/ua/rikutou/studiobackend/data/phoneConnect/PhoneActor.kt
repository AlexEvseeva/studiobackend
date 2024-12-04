package ua.rikutou.studiobackend.data.phoneConnect

import kotlinx.serialization.Serializable

@Serializable
data class PhoneActor(
    val actorId: Int,
    val phoneId: Int,
)
