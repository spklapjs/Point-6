package com.example.point_6

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.point_6.data.repository.SensorRepositoryImpl
import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sensorRepository: SensorRepositoryImpl

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.entries.forEach {
            if (!it.value) allGranted = false
        }
        if (allGranted) {
            startSensorTest()
        } else {
            Log.e("MainActivity", "Permissions not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermissionsAndStart()
    }

    private fun checkPermissionsAndStart() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startSensorTest()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startSensorTest() {
        val phoneManager = PhoneSensorManager(this)
        val spenManager = SpenManager(this)
        sensorRepository = SensorRepositoryImpl(phoneManager, spenManager)

        sensorRepository.startCollection()

        var lastLogTime = 0L

        lifecycleScope.launch {
            sensorRepository.sensorDataFlow.collect { data ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLogTime > 100) {
                    Log.d("SensorTest", "SPen Delta: ${data.spenDelta.contentToString()}")
                    lastLogTime = currentTime
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sensorRepository.isInitialized) {
            sensorRepository.stopCollection()
        }
    }
}