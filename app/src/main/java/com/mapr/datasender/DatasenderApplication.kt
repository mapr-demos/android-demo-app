package com.mapr.datasender

import android.app.Application
import com.mapr.datasender.dagger.AppComponent
import com.mapr.datasender.dagger.AppModule
import com.mapr.datasender.dagger.DaggerAppComponent

class DatasenderApplication : Application() {
    private lateinit var sensorListenerComponent: AppComponent

    private fun initDagger(app: DatasenderApplication): AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(app))
            .build()

    override fun onCreate() {
        super.onCreate()
        sensorListenerComponent = initDagger(this)
    }
}
