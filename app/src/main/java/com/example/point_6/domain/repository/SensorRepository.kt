package com.example.point_6.domain.repository

import com.example.point_6.domain.model.PhoneRawData
import com.example.point_6.domain.model.SPenRawData
import kotlinx.coroutines.flow.SharedFlow

interface SensorRepository {
    val phoneDataStream: SharedFlow<PhoneRawData>
    val spenDataStream: SharedFlow<SPenRawData>

    fun startCollection()
    fun stopCollection()
}