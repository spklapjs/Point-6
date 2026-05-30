package com.example.point_6.domain.model

data class PhoneSensorWindow(
    val startTime: Long,
    val endTime: Long,
    val phoneAccel: FloatArray,
    val phoneGyro: FloatArray
)