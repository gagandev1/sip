package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TiltSensorHelper(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    private var smoothedX = 0f
    private val alpha = 0.15f // Low pass filter factor

    fun startListening() {
        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // event.values[0] is X-axis acceleration: positive is tilted left, negative is tilted right
            val rawX = -event.values[0] / 9.8f
            val clampedX = rawX.coerceIn(-1f, 1f)
            smoothedX += alpha * (clampedX - smoothedX)
            _tiltX.value = smoothedX
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
