package me.cyber.nukleos.dagger

import me.cyber.nukleos.myosensor.MyoConnector
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.control.SensorControlInterface
import me.cyber.nukleos.ui.control.SensorStuffPresenter

@Module
class SensorControlModule {

    @Provides
    fun provideControlDevicePresenter(
            sensorControlView: SensorControlInterface.View,
            myoConnector: MyoConnector,
            sensorStuffManager: SensorStuffManager
    ): SensorStuffPresenter {
        return SensorStuffPresenter(sensorControlView, myoConnector, sensorStuffManager)
    }

}
