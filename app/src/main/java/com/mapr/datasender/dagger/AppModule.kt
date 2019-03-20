package com.mapr.datasender.dagger

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import android.provider.Settings
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.mapr.datasender.DataSenderApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideApplication(app: DataSenderApplication): Context = app

    @Provides
    @Singleton
    fun provideVolley(app: DataSenderApplication): RequestQueue = Volley.newRequestQueue(app)

    @Provides
    @Singleton
    fun sensorManager(app: DataSenderApplication): SensorManager = app.getSystemService(Context.SENSOR_SERVICE)
            as SensorManager

    @SuppressLint("HardwareIds")
    @Provides
    @Singleton
    fun androidId(app: DataSenderApplication): String = Settings.Secure.getString(
        app.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    @Provides
    @Singleton
    fun locationManager(app: DataSenderApplication): LocationManager = app.getSystemService(Context.LOCATION_SERVICE)
            as LocationManager
}