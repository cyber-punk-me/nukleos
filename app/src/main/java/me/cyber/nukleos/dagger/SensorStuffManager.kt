package me.cyber.nukleos.dagger
import android.bluetooth.BluetoothDevice
import com.nilhcem.blefun.common.MotorsInt
import me.cyber.nukleos.myosensor.Myo

class SensorStuffManager {

    var selectedIndex: Int = -1
        set(value) {
            if (value != field) { myo = null }
            field = value
        }

    var findedSensorList: MutableList<BluetoothDevice> = mutableListOf()

    var myo: Myo? = null

    var connected = myo?.isConnected() ?: false

    var motors : MotorsInt? = null

}