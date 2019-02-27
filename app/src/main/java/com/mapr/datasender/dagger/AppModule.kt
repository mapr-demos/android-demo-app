package com.mapr.datasender.dagger

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Application = app

    @Provides
    @Singleton
    fun provideVolley(): RequestQueue = Volley.newRequestQueue(app)

    @Provides
    @Singleton
    fun sensorManager(): SensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun locationManager(): LocationManager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}