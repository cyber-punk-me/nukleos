package me.cyber.nukleos.thing

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import me.cyber.nukleos.IMotors

object MotorsProfile {

    fun createMotorsService(): BluetoothGattService {
        val service = BluetoothGattService(IMotors.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val motorsControlCharacteristic = BluetoothGattCharacteristic(IMotors.CHAR_MOTOR_CONTROL_UUID,
                //Read-write characteristic
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )

        val motorsStateCharacteristic = BluetoothGattCharacteristic(IMotors.CHAR_MOTOR_STATE_UUID,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)
        val configDescriptor = BluetoothGattDescriptor(IMotors.CLIENT_CONFIG_DESCRIPTOR,
                //Read/write descriptor
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        motorsStateCharacteristic.addDescriptor(configDescriptor)

        service.addCharacteristic(motorsControlCharacteristic)
        service.addCharacteristic(motorsStateCharacteristic)

        return service
    }

}