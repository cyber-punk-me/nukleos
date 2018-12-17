package me.cyber.nukleos.thing

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import me.cyber.nukleos.IMotors

object MotorsProfile {

    fun createMotorsService(): BluetoothGattService {
        val service = BluetoothGattService(IMotors.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Local Time Information characteristic
        val motorsCharacteristic = BluetoothGattCharacteristic(IMotors.CHAR_MOTOR_UUID,
                //Read-write characteristic
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )

        service.addCharacteristic(motorsCharacteristic)

        return service
    }

}