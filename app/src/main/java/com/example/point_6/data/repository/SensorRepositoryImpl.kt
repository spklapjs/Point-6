package com.example.point_6.data.repository

import com.example.point_6.domain.repository.SensorRepository
import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import com.example.point_6.data.sensor.BudsManager

class SensorRepositoryImpl(
    private val phoneSensorManager: PhoneSensorManager,
    private val spenManager: SpenManager,
    private val budsManager: BudsManager
) : SensorRepository {

    override fun startCollection() {
        // 하드웨어 매니저들의 센서 수집 시작 로직 호출
    }

    override fun stopCollection() {
        // 하드웨어 매니저들의 센서 수집 중지 로직 호출
    }
}