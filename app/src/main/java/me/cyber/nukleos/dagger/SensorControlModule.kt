package me.cyber.nukleos.dagger

import me.cyber.nukleos.bluetooth.BluetoothConnector
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.control.PeripheryControlInterface
import me.cyber.nukleos.ui.control.PeripheryStuffPresenter

@Module
class SensorControlModule {

    @Provides
    fun provideControlDevicePresenter(
            peripheryControlView: PeripheryControlInterface.View,
            bluetoothConnector: BluetoothConnector,
            peripheryManager: PeripheryManager)
            = PeripheryStuffPresenter(peripheryControlView, bluetoothConnector, peripheryManager)
}
