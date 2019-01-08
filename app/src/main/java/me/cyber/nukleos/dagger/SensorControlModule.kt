package me.cyber.nukleos.dagger

import me.cyber.nukleos.bluetooth.BluetoothConnector
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.control.SensorControlInterface
import me.cyber.nukleos.ui.control.SensorStuffPresenter

@Module
class SensorControlModule {

    @Provides
    fun provideControlDevicePresenter(
            sensorControlView: SensorControlInterface.View,
            bluetoothConnector: BluetoothConnector,
            bluetoothStuffManager: BluetoothStuffManager)
            = SensorStuffPresenter(sensorControlView, bluetoothConnector, bluetoothStuffManager)
}
