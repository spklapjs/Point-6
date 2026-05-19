package com.example.point_6.data.sensor

data class SensorData(
    val timestamp: Long,
    val phoneAccel: FloatArray = FloatArray(3),
    val phoneGyro: FloatArray = FloatArray(3),
    val spenDelta: FloatArray = FloatArray(2)
)