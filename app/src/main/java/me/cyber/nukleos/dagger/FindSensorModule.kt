package me.cyber.nukleos.dagger
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.ui.find.FindSensorInterface
import me.cyber.nukleos.ui.find.FindSensorPresenter

@Module
class FindSensorModule {

    @Provides
    fun provideScanDevicePresenter(
            findSensorView: FindSensorInterface.View,
            bluetoothConnector: BluetoothConnector,
            sensorStuffManager: SensorStuffManager
    ): FindSensorPresenter {
        return FindSensorPresenter(findSensorView, bluetoothConnector, sensorStuffManager)
    }

}
