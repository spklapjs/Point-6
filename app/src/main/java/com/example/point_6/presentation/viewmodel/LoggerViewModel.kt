package com.example.point_6.presentation.viewmodel

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.point_6.data.repository.SensorRepositoryImpl
import com.example.point_6.domain.usecase.DataSyncUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoggerViewModel(
    application: Application,
    private val sensorRepository: SensorRepositoryImpl,
    private val dataSyncUseCase: DataSyncUseCase
) : AndroidViewModel(application) {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordedCount = MutableStateFlow(0)
    val recordedCount: StateFlow<Int> = _recordedCount

    private var recordingJob: Job? = null
    private var csvWriter: FileWriter? = null

    fun startRecording(label: String) {
        if (_isRecording.value) return

        _isRecording.value = true
        _recordedCount.value = 0
        sensorRepository.startCollection()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Point6_${label}_${timestamp}.csv"
        val directory = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, fileName)

        try {
            csvWriter = FileWriter(file).apply {
                append("startTime,endTime,phoneAccel,phoneGyro,spenDelta,label\n")
            }
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "File creation failed: ${e.message}")
            stopRecording()
            return
        }

        recordingJob = viewModelScope.launch {
            dataSyncUseCase.processSensorData(sensorRepository.sensorDataFlow).collect { window ->
                _recordedCount.value += 1
                try {
                    val accelStr = window.phoneAccel.joinToString(";")
                    val gyroStr = window.phoneGyro.joinToString(";")
                    val spenStr = window.spenDelta.joinToString(";")

                    csvWriter?.append("${window.startTime},${window.endTime},${accelStr},${gyroStr},${spenStr},${label}\n")
                    csvWriter?.flush()
                } catch (e: Exception) {
                    Log.e("LoggerViewModel", "Error writing to file: ${e.message}")
                }
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return

        _isRecording.value = false
        sensorRepository.stopCollection()
        recordingJob?.cancel()

        try {
            csvWriter?.close()
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "Error closing file: ${e.message}")
        }
        csvWriter = null
    }
}