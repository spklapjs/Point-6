package com.example.point_6.domain.repository

interface SensorRepository {
    fun startCollection()
    fun stopCollection()
    // 추후 8축 센서 데이터 스트림 및 200ms 윈도우 슬라이싱 결과 반환 로직 추가 예정
}