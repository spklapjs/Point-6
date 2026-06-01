package com.example.point_6.domain.usecase

import com.spklapjs.point_6.data.inference.SmartphoneInferenceEngine
import com.spklapjs.point_6.data.inference.SPenInferenceEngine
import com.example.point_6.domain.model.DrumType
import com.example.point_6.domain.model.PhoneSensorWindow
import com.example.point_6.domain.model.SPenSensorWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetInferenceUseCase(
    private val smartphoneEngine: SmartphoneInferenceEngine,
    private val spenEngine: SPenInferenceEngine
) {
    suspend fun execute(
        phoneWindow: PhoneSensorWindow?,
        spenWindow: SPenSensorWindow?
    ): Pair<DrumType?, DrumType?> = coroutineScope {

        val phoneResult = async(Dispatchers.Default) {
            if (phoneWindow != null && phoneWindow.phoneAccel.size == 120 && phoneWindow.phoneGyro.size == 120) {
                val index = smartphoneEngine.predict(phoneWindow)
                DrumType.fromIndex(index)
            } else {
                null
            }
        }

        val spenResult = async(Dispatchers.Default) {
            if (spenWindow != null && spenWindow.spenDelta.size == 80) {
                val index = spenEngine.predict(spenWindow)
                DrumType.fromIndex(index)
            } else {
                null
            }
        }

        Pair(phoneResult.await(), spenResult.await())
    }
}