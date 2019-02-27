package com.mapr.datasender.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import javax.inject.Inject

class SensorService(var url: String) : SensorEventListener {

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val body = JSONObject()
            body.put("accelerometer", event.values)
            val request = JsonObjectRequest(Request.Method.PUT, url, body, { }, { })
            requestQueue.add(request)
        }
    }
}