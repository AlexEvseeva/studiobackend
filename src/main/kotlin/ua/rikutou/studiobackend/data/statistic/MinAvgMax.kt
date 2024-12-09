package ua.rikutou.studiobackend.data.statistic

import kotlinx.serialization.Serializable

@Serializable
data class MinAvgMax(
    val min:Int,
    val avg:Int,
    val max:Int
)
