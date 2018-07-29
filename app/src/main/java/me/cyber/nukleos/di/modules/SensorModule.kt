package me.cyber.nukleos.di.modules

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.sensor.ISensor
import me.cyber.nukleos.sensor.Sensor
import javax.inject.Singleton

@Module
class SensorModule {

    @Provides
    @Singleton
    fun provideSensor(): ISensor = Sensor()
}