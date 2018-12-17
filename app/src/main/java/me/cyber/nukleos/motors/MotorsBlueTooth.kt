package me.cyber.nukleos.motors

import android.bluetooth.*
import android.content.Context
import android.util.Log
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.myosensor.TAG

class MotorsBlueTooth(private val device: BluetoothDevice) : IMotors, BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null
    var servicesDiscovered = false

    override fun connect(context: Any) {
        if (gatt == null) {
            gatt = device.connectGatt(context as Context, false, this)
        }
    }

    fun disconnect() {
        gatt?.close()
        gatt = null
        servicesDiscovered = false
    }

    override fun spinMotor(iMotor: Byte, direction: Byte, speed: Byte) {
        val characteristic = gatt
                ?.getService(IMotors.SERVICE_UUID)
                ?.getCharacteristic(IMotors.CHAR_MOTOR_UUID)
        val command = ByteArray(3)
        command[0] = iMotor
        command[1] = direction
        command[2] = speed
        characteristic?.value = command
        gatt?.writeCharacteristic(characteristic)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange: $status -> $newState")
        if (newState != status && newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "MotorsBlueTooth Connected")
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // Calling disconnect() here will cause to release the GATT resources.
            disconnect()
            Log.d(TAG, "Bluetooth Disconnected")
        }
    }


    @Synchronized
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS && !servicesDiscovered) {
            servicesDiscovered = true
            Log.d(TAG, "Spinning motor..")

            Thread{
                spinMotor(1, IMotors.FORWARD, 100)
            }.start()
/*            val service = gatt.getService(IMotors.SERVICE_UUID)
            if (service != null) {
                val characteristic = service.getCharacteristic(IMotors.CHAR_MOTOR_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                }
            }*/
            Log.d(TAG, "MotorsBlueTooth connected : $servicesDiscovered")

        } else {
            Log.w(TAG, "onServicesDiscovered received: $status")
        }
    }

    override fun stopMotors() {
        spinMotor(0, 0, 0)
    }

}