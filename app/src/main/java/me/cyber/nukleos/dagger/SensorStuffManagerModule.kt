package me.cyber.nukleos.dagger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ContextModule::class])
class SensorStuffManagerModule {

    @Provides
    @Singleton
    fun provideSensorStuffManager(): BluetoothStuffManager {
        return BluetoothStuffManager()
    }

}
