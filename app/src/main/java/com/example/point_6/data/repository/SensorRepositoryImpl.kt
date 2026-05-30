package com.example.point_6.data.repository

import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import com.example.point_6.domain.model.PhoneRawData
import com.example.point_6.domain.model.SPenRawData
import com.example.point_6.domain.repository.SensorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SensorRepositoryImpl(
    private val phoneSensorManager: PhoneSensorManager,
    private val spenManager: SpenManager
) : SensorRepository {

    private val _phoneDataStream = MutableSharedFlow<PhoneRawData>(extraBufferCapacity = 200)
    override val phoneDataStream: SharedFlow<PhoneRawData> = _phoneDataStream.asSharedFlow()

    private val _spenDataStream = MutableSharedFlow<SPenRawData>(extraBufferCapacity = 200)
    override val spenDataStream: SharedFlow<SPenRawData> = _spenDataStream.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // 폰 센서 매니저로부터 데이터가 들어오면 시스템 시간을 생성하여 독립적인 폰 스트림으로 방출
        phoneSensorManager.onSensorDataReceived = { accel, gyro ->
            scope.launch {
                val timestamp = System.currentTimeMillis()
                _phoneDataStream.emit(PhoneRawData(timestamp, accel, gyro))
            }
        }

        // 에스펜 매니저로부터 데이터가 들어오면 독립적인 에스펜 스트림으로 방출
        spenManager.onSpenDataReceived = { deltaX, deltaY ->
            scope.launch {
                _spenDataStream.emit(SPenRawData(System.currentTimeMillis(), deltaX, deltaY))
            }
        }
    }

    override fun startCollection() {
        phoneSensorManager.startListening()
        // 에스펜 리모트 연결 활성화 로직이 추가로 필요하다면 여기에 작성
    }

    override fun stopCollection() {
        phoneSensorManager.stopListening()
        // 에스펜 리모트 연결 해제 로직이 추가로 필요하다면 여기에 작성
    }
}