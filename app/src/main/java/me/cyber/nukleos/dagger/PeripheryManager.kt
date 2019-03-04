package me.cyber.nukleos.dagger
import android.bluetooth.BluetoothDevice
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.myosensor.Myo
import me.cyber.nukleos.synaps.UsbHandler

class PeripheryManager {

    var selectedIndex: Int = -1
        set(value) {
            if (value != field) { myo = null }
            field = value
        }

    var foundBTDevicesList: MutableList<BluetoothDevice> = mutableListOf()

    var myo: Myo? = null

    var synapsUsbHandler: UsbHandler? = null

    @Volatile var motors : IMotors? = null

    fun clear() {
        selectedIndex = -1
        foundBTDevicesList.clear()
    }
}