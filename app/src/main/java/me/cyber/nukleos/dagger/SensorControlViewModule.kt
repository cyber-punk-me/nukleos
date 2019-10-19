package me.cyber.nukleos.dagger
import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.control.PeripheryControlInterface
import me.cyber.nukleos.ui.control.PeripheryControlFragment

@Module
abstract class SensorControlViewModule {

    @Binds
    abstract fun provideSensorControlView(exportFragment: PeripheryControlFragment): PeripheryControlInterface.View
}