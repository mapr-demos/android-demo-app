package com.mapr.datasender.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.LocationManager
import android.support.v4.content.ContextCompat
import com.android.volley.RequestQueue
import com.mapr.datasender.config.Config
import com.mapr.datasender.request.RequestFormatUtility.getCredentials
import com.mapr.datasender.request.RequestFormatUtility.getUrl
import com.mapr.datasender.request.RequestTag
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorsService @Inject constructor(
    private val requestQueue: RequestQueue, private val sensorManager: SensorManager,
    private val locationManager: LocationManager, private val context: Context, private val androidId: String
) {
    private val log = Logger.getLogger(SensorsService::class.java.name)

    var connected: Boolean = false
    private lateinit var accelerometerService: AccelerometerService
    private var locationService: LocationService? = null

    fun start(config: Config, onError: (String) -> Unit) {
        log.info("Start sensors")
        startAccelerometer(config, onError)
        startLocation(config, onError)
        connected = true
    }

    private fun startAccelerometer(config: Config, onError: (String) -> Unit) {
        accelerometerService = AccelerometerService(
            getUrl(config, androidId, RequestTag.ACCELEROMETER.tag),
            getCredentials(config),
            requestQueue, sensorManager, androidId, onError
        )
        accelerometerService.start()
    }

    private fun startLocation(config: Config, onError: (String) -> Unit) {
        if (checkLocationPermission()) {
            locationService = LocationService(
                getUrl(config, androidId, RequestTag.LOCATION.tag),
                getCredentials(config),
                requestQueue, locationManager, androidId, onError
            )
            locationService!!.start()
        } else {
            locationService = null
        }
    }

    fun stop() {
        requestQueue.cancelAll(RequestTag.ACCELEROMETER)
        requestQueue.cancelAll(RequestTag.LOCATION)

        log.info("Stop sensors")
        accelerometerService.stop()
        log.info("Stop accelerometer")
        locationService?.stop()
        log.info("Stop navigation")

        connected = false
    }

    private fun checkLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

}