package com.example.point_6.data.repository

import com.example.point_6.domain.repository.SensorRepository
import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import com.example.point_6.data.sensor.SensorData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SensorRepositoryImpl(
    private val phoneSensorManager: PhoneSensorManager,
    private val spenManager: SpenManager
) : SensorRepository {

    private val _sensorDataFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 64)
    val sensorDataFlow: SharedFlow<SensorData> = _sensorDataFlow.asSharedFlow()

    override fun startCollection() {
        var currentSpenDelta = FloatArray(2)

        spenManager.onSpenDataReceived = { deltaX, deltaY ->
            currentSpenDelta = floatArrayOf(deltaX, deltaY)
        }

        phoneSensorManager.onSensorDataReceived = { accel, gyro ->
            val data = SensorData(
                timestamp = System.currentTimeMillis(),
                phoneAccel = accel,
                phoneGyro = gyro,
                spenDelta = currentSpenDelta
            )
            _sensorDataFlow.tryEmit(data)
        }

        phoneSensorManager.startListening()
        spenManager.connectSpen()
    }

    override fun stopCollection() {
        phoneSensorManager.stopListening()
        spenManager.disconnectSpen()
    }
}