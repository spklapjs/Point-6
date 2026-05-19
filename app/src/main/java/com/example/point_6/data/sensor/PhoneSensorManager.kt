package com.example.point_6.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class PhoneSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var onSensorDataReceived: ((FloatArray, FloatArray) -> Unit)? = null
    private var currentAccel = FloatArray(3)
    private var currentGyro = FloatArray(3)

    fun startListening() {
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        gyroSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> currentAccel = it.values.clone()
                Sensor.TYPE_GYROSCOPE -> currentGyro = it.values.clone()
            }
            onSensorDataReceived?.invoke(currentAccel, currentGyro)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}