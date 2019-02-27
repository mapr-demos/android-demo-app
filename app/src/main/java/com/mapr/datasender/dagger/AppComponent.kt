package com.mapr.datasender.dagger

import com.mapr.datasender.sensors.SensorService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent{
    fun inject(target: SensorService)
}
