package me.cyber.nukleos.dagger
import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.control.SensorControlInterface
import me.cyber.nukleos.ui.control.SensorControlFragment

@Module
abstract class SensorControlViewModule {

    @Binds
    abstract fun provideSensorControlView(exportFragment: SensorControlFragment): SensorControlInterface.View
}