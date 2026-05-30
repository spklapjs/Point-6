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
        phoneSensorManager.onSensorDataReceived = { accel, gyro ->
            scope.launch {
                val timestamp = System.currentTimeMillis()
                _phoneDataStream.emit(PhoneRawData(timestamp, accel, gyro))
            }
        }

        spenManager.onSpenDataReceived = { deltaX, deltaY ->
            scope.launch {
                _spenDataStream.emit(SPenRawData(System.currentTimeMillis(), deltaX, deltaY))
            }
        }
    }

    override fun startCollection() {
        phoneSensorManager.startListening()
        spenManager.connectSpen()
    }

    override fun stopCollection() {
        phoneSensorManager.stopListening()
        spenManager.disconnectSpen()
    }
}