package com.example.point_6.domain.model

data class SensorWindow(
    val startTime: Long,
    val endTime: Long,
    val phoneAccel: FloatArray,
    val phoneGyro: FloatArray,
    val spenDelta: FloatArray
)
