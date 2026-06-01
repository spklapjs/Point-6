package com.example.point_6.domain.usecase

import com.example.point_6.domain.model.PhoneRawData
import com.example.point_6.domain.model.PhoneSensorWindow
import com.example.point_6.domain.model.SPenRawData
import com.example.point_6.domain.model.SPenSensorWindow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataSyncUseCase {

    private val windowSizeMs = 400L
    private val overlapMs = 200L

    fun processPhoneDataStream(dataStream: Flow<PhoneRawData>): Flow<PhoneSensorWindow> = flow {
        val buffer = mutableListOf<PhoneRawData>()
        var lastWindowTime = 0L

        dataStream.collect { data ->
            buffer.add(data)

            val currentTime = data.timestamp
            if (lastWindowTime == 0L) {
                lastWindowTime = currentTime
            }

            if (currentTime - lastWindowTime >= overlapMs) {
                val windowData = buffer.filter { it.timestamp >= currentTime - windowSizeMs }

                if (windowData.isNotEmpty()) {
                    val window = formatPhoneWindow(windowData, currentTime - windowSizeMs, currentTime)
                    emit(window)
                }

                buffer.removeAll { it.timestamp < currentTime - windowSizeMs }
                lastWindowTime = currentTime
            }
        }
    }

    fun processSPenDataStream(dataStream: Flow<SPenRawData>): Flow<SPenSensorWindow> = flow {
        val buffer = mutableListOf<SPenRawData>()
        var lastWindowTime = 0L

        dataStream.collect { data ->
            buffer.add(data)

            val currentTime = data.timestamp
            if (lastWindowTime == 0L) {
                lastWindowTime = currentTime
            }

            if (currentTime - lastWindowTime >= overlapMs) {
                val windowData = buffer.filter { it.timestamp >= currentTime - windowSizeMs }

                if (windowData.isNotEmpty()) {
                    val window = formatSPenWindow(windowData, currentTime - windowSizeMs, currentTime)
                    emit(window)
                }

                buffer.removeAll { it.timestamp < currentTime - windowSizeMs }
                lastWindowTime = currentTime
            }
        }
    }

    private fun formatPhoneWindow(data: List<PhoneRawData>, startTime: Long, endTime: Long): PhoneSensorWindow {
        val size = data.size
        val pAccel = FloatArray(3 * size)
        val pGyro = FloatArray(3 * size)

        for (i in 0 until size) {
            val current = data[i]
            pAccel[i * 3] = current.accel[0]
            pAccel[i * 3 + 1] = current.accel[1]
            pAccel[i * 3 + 2] = current.accel[2]

            pGyro[i * 3] = current.gyro[0]
            pGyro[i * 3 + 1] = current.gyro[1]
            pGyro[i * 3 + 2] = current.gyro[2]
        }

        return PhoneSensorWindow(
            startTime = startTime,
            endTime = endTime,
            phoneAccel = pAccel,
            phoneGyro = pGyro
        )
    }

    private fun formatSPenWindow(data: List<SPenRawData>, startTime: Long, endTime: Long): SPenSensorWindow {
        val size = data.size
        val sDelta = FloatArray(2 * size)

        for (i in 0 until size) {
            val current = data[i]
            sDelta[i * 2] = current.deltaX
            sDelta[i * 2 + 1] = current.deltaY
        }

        return SPenSensorWindow(
            startTime = startTime,
            endTime = endTime,
            spenDelta = sDelta
        )
    }
}