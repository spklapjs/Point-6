package com.example.point_6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.point_6.domain.model.DrumType
import com.example.point_6.domain.model.PhoneSensorWindow
import com.example.point_6.domain.model.SPenSensorWindow
import com.example.point_6.domain.usecase.GetInferenceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val getInferenceUseCase: GetInferenceUseCase
) : ViewModel() {

    private val _inferenceResult = MutableSharedFlow<Pair<DrumType?, DrumType?>>()
    val inferenceResult: SharedFlow<Pair<DrumType?, DrumType?>> = _inferenceResult.asSharedFlow()

    fun processSensorData(phoneWindow: PhoneSensorWindow?, spenWindow: SPenSensorWindow?) {
        viewModelScope.launch {
            val result = getInferenceUseCase.execute(phoneWindow, spenWindow)
            _inferenceResult.emit(result)
        }
    }
}