package com.mapr.datasender.ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Button
import com.mapr.datasender.R
import com.mapr.datasender.location.LocationService
import com.mapr.datasender.sensors.SensorService
import javax.inject.Inject

class HomepageActivity : Activity() {

    @Inject
    lateinit var sensorManager: SensorManager
    @Inject
    lateinit var locationManager: LocationManager
    @Inject
    lateinit var context: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button_1)
        button.setOnClickListener {
            val accelerometer: Sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).first()
            sensorManager.registerListener(SensorService("http://localhost:8080/accelerometer"), accelerometer, 1000)
            if (checkLocationPermission()) {
                val locationService = LocationService("http://localhost:8080/accelerometer")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, locationService)
            }
        }
    }

    private fun checkLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}