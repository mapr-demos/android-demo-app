package com.mapr.datasender.sensors

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.mapr.datasender.request.CustomJsonObjectRequestBasicAuth
import com.mapr.datasender.request.RequestTag
import org.json.JSONObject
import java.util.*
import java.util.logging.Logger

import java.lang.String.format

class LocationService(
    private var url: String,
    private var credentials: String,
    private var requestQueue: RequestQueue,
    private val locationManager: LocationManager,
    private val androidId: String,
    private val onError: (String) -> Unit
) : LocationListener {
    private val log = Logger.getLogger(LocationService::class.java.name)
    private var enabled = false

    @SuppressLint("MissingPermission")
    fun start() {
        enabled = true
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000, 0f,
            this
        )
    }

    fun stop() {
        enabled = false
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location?) {
        if (enabled) {
            convertAndSend(location?.latitude, location?.longitude)
        } else {
            log.warning("Failed to unregister, try again")
            locationManager.removeUpdates(this)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertAndSend(latitude: Double?, longitude: Double?) {
        val body = JSONObject()
        body.put("androidId", androidId)
        body.put("timestamp", Date().toInstant().epochSecond)
        body.put("latitude", latitude)
        body.put("longitude", longitude)
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
        request.tag = RequestTag.LOCATION
        requestQueue.add(request)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}