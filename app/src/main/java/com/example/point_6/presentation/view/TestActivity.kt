package com.example.point_6.presentation.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.point_6.R
import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import com.spklapjs.point_6.data.inference.SmartphoneInferenceEngine
import com.spklapjs.point_6.data.inference.SPenInferenceEngine
import com.example.point_6.domain.model.PhoneSensorWindow
import com.example.point_6.domain.model.SPenSensorWindow
import com.example.point_6.domain.usecase.GetInferenceUseCase
import com.example.point_6.presentation.feedback.AudioEngine
import com.example.point_6.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class TestActivity : AppCompatActivity() {

    private lateinit var phoneSensorManager: PhoneSensorManager
    private lateinit var spenManager: SpenManager
    private lateinit var gameViewModel: GameViewModel
    private lateinit var audioEngine: AudioEngine

    private val phoneAccelBuffer = mutableListOf<FloatArray>()
    private val phoneGyroBuffer = mutableListOf<FloatArray>()
    private var phoneFrameCount = 0
    private var phoneCooldown = 0

    private val spenDeltaBuffer = mutableListOf<FloatArray>()
    private var spenFrameCount = 0
    private var spenCooldown = 0

    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        tvResult = findViewById(R.id.tvResult)

        val phoneEngine = SmartphoneInferenceEngine(this)
        val spenEngine = SPenInferenceEngine(this)
        val useCase = GetInferenceUseCase(phoneEngine, spenEngine)

        gameViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(useCase) as T
            }
        })[GameViewModel::class.java]

        phoneSensorManager = PhoneSensorManager(this)
        spenManager = SpenManager(this)
        audioEngine = AudioEngine(this)

        setupSensors()
        observeInferenceResults()
    }

    private fun setupSensors() {
        phoneSensorManager.onSensorDataReceived = { accel, gyro ->
            if (phoneAccelBuffer.size >= 40) phoneAccelBuffer.removeAt(0)
            if (phoneGyroBuffer.size >= 40) phoneGyroBuffer.removeAt(0)

            phoneAccelBuffer.add(accel.clone())
            phoneGyroBuffer.add(gyro.clone())

            phoneFrameCount++
            if (phoneFrameCount >= 5) {
                phoneFrameCount = 0
                triggerPhoneInference()
            }
        }

        spenManager.onSpenDataReceived = { dx, dy ->
            if (spenDeltaBuffer.size >= 40) spenDeltaBuffer.removeAt(0)
            spenDeltaBuffer.add(floatArrayOf(dx, dy))

            spenFrameCount++
            if (spenFrameCount >= 5) {
                spenFrameCount = 0
                triggerSpenInference()
            }
        }
    }

    private fun triggerPhoneInference() {
        if (phoneCooldown > 0) {
            phoneCooldown--
            return
        }

        if (phoneAccelBuffer.size == 40 && phoneGyroBuffer.size == 40) {
            var maxZ = 0f
            var peakIndex = -1

            for (i in 0 until 40) {
                val absZ = abs(phoneAccelBuffer[i][2])
                if (absZ > maxZ) {
                    maxZ = absZ
                    peakIndex = i
                }
            }

            if (maxZ > 35f && peakIndex in 25..38) {
                val flatAccel = FloatArray(120)
                val flatGyro = FloatArray(120)
                for (i in 0 until 40) {
                    System.arraycopy(phoneAccelBuffer[i], 0, flatAccel, i * 3, 3)
                    System.arraycopy(phoneGyroBuffer[i], 0, flatGyro, i * 3, 3)
                }
                val phoneWindow = PhoneSensorWindow(0L, 0L, flatAccel, flatGyro)
                gameViewModel.processSensorData(phoneWindow, null)
                phoneCooldown = 50
            }
        }
    }

    private fun triggerSpenInference() {
        if (spenCooldown > 0) {
            spenCooldown--
            return
        }

        if (spenDeltaBuffer.size == 40) {
            var maxDelta = 0f
            var peakIndex = -1

            for (i in 0 until 40) {
                val absX = abs(spenDeltaBuffer[i][0])
                val absY = abs(spenDeltaBuffer[i][1])
                val currentMax = if (absX > absY) absX else absY
                if (currentMax > maxDelta) {
                    maxDelta = currentMax
                    peakIndex = i
                }
            }

            if (maxDelta > 0.2f && peakIndex in 25..38) {
                val flatDelta = FloatArray(80)
                for (i in 0 until 40) {
                    System.arraycopy(spenDeltaBuffer[i], 0, flatDelta, i * 2, 2)
                }
                val spenWindow = SPenSensorWindow(0L, 0L, flatDelta)
                gameViewModel.processSensorData(null, spenWindow)
                spenCooldown = 3
            }
        }
    }

    private fun observeInferenceResults() {
        lifecycleScope.launch {
            gameViewModel.inferenceResult.collectLatest { result ->
                val phoneDrum = result.first
                val spenDrum = result.second

                if (phoneDrum != null) {
                    audioEngine.play(phoneDrum)
                }
                if (spenDrum != null) {
                    audioEngine.play(spenDrum)
                }

                val phoneResult = phoneDrum?.name ?: "None"
                val spenResult = spenDrum?.name ?: "None"
                tvResult.text = "Phone: $phoneResult \nS-Pen: $spenResult"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        phoneSensorManager.startListening()
        spenManager.connectSpen()
    }

    override fun onPause() {
        super.onPause()
        phoneSensorManager.stopListening()
        spenManager.disconnectSpen()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}