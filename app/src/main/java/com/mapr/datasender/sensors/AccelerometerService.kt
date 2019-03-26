package com.mapr.datasender.sensors

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.mapr.datasender.request.CustomJsonObjectRequestBasicAuth
import com.mapr.datasender.request.RequestTag
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.logging.Logger

import java.lang.String.format

class AccelerometerService(
    private var url: String, private var credentials: String, private var requestQueue: RequestQueue,
    private val sensorManager: SensorManager, private val androidId: String, private val onError: (String) -> Unit
) : SensorEventListener {
    private val log = Logger.getLogger(AccelerometerService::class.java.name)
    private var enabled = false

    fun start() {
        enabled = true
        val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, 1000000)
    }

    fun stop() {
        enabled = false
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (enabled) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                convertAndSend(event)
            }
        } else {
            log.warning("Failed to unregister, try again")
            sensorManager.unregisterListener(this)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertAndSend(event: SensorEvent?) {
        val body = JSONObject()
        body.put("androidId", androidId)
        body.put("timestamp", Date().toInstant().epochSecond)
        body.put("accelerometer", JSONArray(event!!.values))
        val request = CustomJsonObjectRequestBasicAuth(
            Request.Method.PUT, url, body,
            Response.Listener { response ->
                if (response != null)
                    log.info(response.toString())
            },
            Response.ErrorListener { error ->
                run {
                    if (error.message != null) {
                        log.warning(error.message.toString())
                        onError(error.message.toString())
                    } else {
                        val msg: String =
                            format("Failed to connect, code: %d", error.networkResponse?.statusCode)
                        log.warning(msg)
                        onError(msg)
                    }
                }
            }, credentials
        )
        request.tag = RequestTag.ACCELEROMETER
        requestQueue.add(request)
    }

}