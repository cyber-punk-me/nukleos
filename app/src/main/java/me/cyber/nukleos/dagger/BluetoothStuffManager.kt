package me.cyber.nukleos.dagger
import android.bluetooth.BluetoothDevice
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.myosensor.Myo

class BluetoothStuffManager {

    var selectedIndex: Int = -1
        set(value) {
            if (value != field) { myo = null }
            field = value
        }

    var foundBTDevicesList: MutableList<BluetoothDevice> = mutableListOf()

    var myo: Myo? = null

    var connected = myo?.isConnected() ?: false

    @Volatile var motors : IMotors? = null

}