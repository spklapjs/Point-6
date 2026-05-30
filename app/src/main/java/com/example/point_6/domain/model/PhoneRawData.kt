package com.example.point_6.domain.model

data class PhoneRawData(
    val timestamp: Long,
    val accel: FloatArray,
    val gyro: FloatArray
)