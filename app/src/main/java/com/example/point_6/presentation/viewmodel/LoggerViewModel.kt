package com.example.point_6.presentation.viewmodel

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.point_6.data.repository.SensorRepositoryImpl
import com.example.point_6.domain.model.PhoneRawData
import com.example.point_6.domain.model.SPenRawData
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
    private val sensorRepository: SensorRepositoryImpl
) : AndroidViewModel(application) {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordedCount = MutableStateFlow(0)
    val recordedCount: StateFlow<Int> = _recordedCount

    private val _phoneStream = MutableStateFlow<PhoneRawData?>(null)
    val phoneStream: StateFlow<PhoneRawData?> = _phoneStream

    private val _spenStream = MutableStateFlow<SPenRawData?>(null)
    val spenStream: StateFlow<SPenRawData?> = _spenStream

    private var csvWriter: FileWriter? = null
    private var currentLabel: String = ""

    init {
        sensorRepository.startCollection()
        observeSensorStreams()
    }

    private fun observeSensorStreams() {
        viewModelScope.launch {
            launch {
                sensorRepository.phoneDataStream.collect { data ->
                    _phoneStream.value = data
                    if (_isRecording.value && currentLabel.startsWith("Phone")) {
                        writePhoneData(data)
                    }
                }
            }
            launch {
                sensorRepository.spenDataStream.collect { data ->
                    _spenStream.value = data
                    if (_isRecording.value && currentLabel.startsWith("SPen")) {
                        writeSPenData(data)
                    }
                }
            }
        }
    }

    fun startRecording(label: String) {
        if (_isRecording.value) return
        currentLabel = label
        _isRecording.value = true
        _recordedCount.value = 0

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${label}_${timestamp}.csv"

        val baseDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val folderName = if (label.startsWith("Phone")) "phone" else "spen"
        val directory = File(baseDir, folderName)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)

        try {
            csvWriter = FileWriter(file).apply {
                if (label.startsWith("Phone")) {
                    append("timestamp,accelX,accelY,accelZ,gyroX,gyroY,gyroZ,label\n")
                } else {
                    append("timestamp,deltaX,deltaY,label\n")
                }
            }
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "File creation failed: ${e.message}")
            stopRecording()
        }
    }

    private fun writePhoneData(data: PhoneRawData) {
        try {
            csvWriter?.append("${data.timestamp},${data.accel[0]},${data.accel[1]},${data.accel[2]},${data.gyro[0]},${data.gyro[1]},${data.gyro[2]},$currentLabel\n")
            csvWriter?.flush()
            _recordedCount.value += 1
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "Error writing phone data: ${e.message}")
        }
    }

    private fun writeSPenData(data: SPenRawData) {
        try {
            csvWriter?.append("${data.timestamp},${data.deltaX},${data.deltaY},$currentLabel\n")
            csvWriter?.flush()
            _recordedCount.value += 1
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "Error writing spen data: ${e.message}")
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false

        try {
            csvWriter?.close()
        } catch (e: Exception) {
            Log.e("LoggerViewModel", "Error closing file: ${e.message}")
        }
        csvWriter = null
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stopCollection()
    }
}