package me.cyber.nukleos.motors

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.nilhcem.blefun.common.AwesomenessProfile.*
import com.nilhcem.blefun.common.MotorsInt
import me.cyber.nukleos.myosensor.TAG

class Motors(private val device: BluetoothDevice) : MotorsInt, BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null

    override fun connect(context: Any?) {
        gatt = device.connectGatt(context as Context, false, this)
    }

    fun disconnect() {
        gatt?.close()
    }

    override fun spinMotor(iMotor: Byte, direction: Byte, speed: Byte) {
        val characteristic = gatt
                ?.getService(SERVICE_UUID)
                ?.getCharacteristic(CHARACTERISTIC_INTERACTOR_UUID)
        val command = ByteArray(3)
        command[0] = iMotor
        command[1] = direction
        command[2] = speed
        characteristic?.setValue("!")//command
        gatt?.writeCharacteristic(characteristic)
    }

    fun writeInteractor() {
        val interactor = gatt
                ?.getService(SERVICE_UUID)
                ?.getCharacteristic(CHARACTERISTIC_INTERACTOR_UUID)
        interactor?.setValue("!")
        gatt?.writeCharacteristic(interactor)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange: $status -> $newState")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "Motors Connected")
            //spinMotor(1, MotorsInt.FORWARD, 100)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // Calling disconnect() here will cause to release the GATT resources.
            disconnect()
            Log.d(TAG, "Bluetooth Disconnected")
        }
    }


    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            var connected = false

            val service = gatt.getService(SERVICE_UUID)
            if (service != null) {
                val characteristic = service.getCharacteristic(CHARACTERISTIC_COUNTER_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)

                    val descriptor = characteristic.getDescriptor(DESCRIPTOR_CONFIG)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        connected = gatt.writeDescriptor(descriptor)
                    }
                }
            }
            Log.d(TAG,"Motors connected : $connected")
            if (connected) {
                writeInteractor()
            }
        } else {
            Log.w(TAG, "onServicesDiscovered received: $status")
        }
    }

    override fun stopMotors() {
        spinMotor(0, 0, 0)
    }

    companion object {
        private val MOTOR_COUNT: Int = 4
    }

}