package com.example.point_6.domain.usecase

import com.example.point_6.data.sensor.SensorData
import com.example.point_6.domain.model.SensorWindow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataSyncUseCase {

    private val windowSizeMs = 200L
    private val overlapMs = 100L
    private val buffer = mutableListOf<SensorData>()
    private var lastWindowTime = 0L

    fun processSensorData(dataStream: Flow<SensorData>): Flow<SensorWindow> = flow {
        dataStream.collect { data ->
            buffer.add(data)

            val currentTime = data.timestamp
            if (lastWindowTime == 0L) {
                lastWindowTime = currentTime
            }

            if (currentTime - lastWindowTime >= overlapMs) {
                val windowData = buffer.filter { it.timestamp >= currentTime - windowSizeMs }

                if (windowData.isNotEmpty()) {
                    val window = interpolateAndFormat(windowData, currentTime - windowSizeMs, currentTime)
                    emit(window)
                }

                buffer.removeAll { it.timestamp < currentTime - windowSizeMs }
                lastWindowTime = currentTime
            }
        }
    }

    private fun interpolateAndFormat(data: List<SensorData>, startTime: Long, endTime: Long): SensorWindow {
        val size = data.size
        val pAccel = FloatArray(3 * size)
        val pGyro = FloatArray(3 * size)
        val sDelta = FloatArray(2 * size)

        for (i in 0 until size) {
            val current = data[i]
            pAccel[i * 3] = current.phoneAccel[0]
            pAccel[i * 3 + 1] = current.phoneAccel[1]
            pAccel[i * 3 + 2] = current.phoneAccel[2]

            pGyro[i * 3] = current.phoneGyro[0]
            pGyro[i * 3 + 1] = current.phoneGyro[1]
            pGyro[i * 3 + 2] = current.phoneGyro[2]

            sDelta[i * 2] = current.spenDelta[0]
            sDelta[i * 2 + 1] = current.spenDelta[1]
        }

        return SensorWindow(
            startTime = startTime,
            endTime = endTime,
            phoneAccel = pAccel,
            phoneGyro = pGyro,
            spenDelta = sDelta
        )
    }
}