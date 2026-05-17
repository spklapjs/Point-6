package com.example.point_6.data.sensor

import android.content.Context
import android.hardware.SensorManager

class PhoneSensorManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun startListening() {
        // 고주파수 가속도계 및 자이로스코프 리스너 등록 로직 구현 예정
    }

    fun stopListening() {
        // 센서 리스너 해제 로직 구현 예정
    }
}