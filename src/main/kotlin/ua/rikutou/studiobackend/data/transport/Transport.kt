package ua.rikutou.studiobackend.data.transport

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Transport(
    val transportId: Int? = null,
    val type: TransportType,
    val mark: String,
    val manufactureDate: Long,
    val seats: Int,
    val departmentId: Int,
    val color: String,
    val technicalState: String,
    val rentPrice: Float
)

@Serializable
enum class TransportType {
    @SerialName("0") Sedan,
    @SerialName("1") Pickup,
    @SerialName("2") Bus,
    @SerialName("3") Commercial;

    fun fromTransportType(): Int =
        when(this) {
            Sedan -> 0
            Pickup -> 1
            Bus -> 2
            Commercial -> 3
        }
}

fun Int.toTransportType(): TransportType =
    when(this) {
        0 -> TransportType.Sedan
        1 -> TransportType.Pickup
        2 -> TransportType.Bus
        3 -> TransportType.Commercial
        else -> throw IllegalArgumentException("Unknown transportType $this")
    }