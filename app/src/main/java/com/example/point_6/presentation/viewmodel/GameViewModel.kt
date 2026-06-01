package com.example.point_6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.point_6.domain.model.DrumType
import com.example.point_6.domain.model.PhoneSensorWindow
import com.example.point_6.domain.model.SPenSensorWindow
import com.example.point_6.domain.usecase.GetInferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val getInferenceUseCase: GetInferenceUseCase
) : ViewModel() {

    private val _inferenceResult = MutableStateFlow<Pair<DrumType?, DrumType?>>(Pair(null, null))
    val inferenceResult: StateFlow<Pair<DrumType?, DrumType?>> = _inferenceResult.asStateFlow()

    fun processSensorData(phoneWindow: PhoneSensorWindow?, spenWindow: SPenSensorWindow?) {
        viewModelScope.launch {
            val result = getInferenceUseCase.execute(phoneWindow, spenWindow)
            _inferenceResult.value = result
        }
    }
}