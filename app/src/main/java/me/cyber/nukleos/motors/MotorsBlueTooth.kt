package me.cyber.nukleos.motors

import android.bluetooth.*
import android.content.Context
import android.util.Log
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.myosensor.TAG
import java.lang.Thread.sleep

class MotorsBlueTooth(private val device: BluetoothDevice) : IMotors, BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null
    var servicesDiscovered = false
    private var mRecievedState: ByteArray = ByteArray(IMotors.MOTORS_COUNT)

    override fun getState() = mRecievedState

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

    override fun spinMotor(iMotor: Byte, speed: Byte) {
        val characteristic = gatt
                ?.getService(IMotors.SERVICE_UUID)
                ?.getCharacteristic(IMotors.CHAR_MOTOR_CONTROL_UUID)
        val command = ByteArray(2)
        command[0] = iMotor
        command[2] = speed
        characteristic?.value = command
        gatt?.writeCharacteristic(characteristic)
    }

    override fun stopMotors() = spinMotor(0, 0)

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
            gatt.getService(IMotors.SERVICE_UUID)?.let {
                val characteristic = it.getCharacteristic(IMotors.CHAR_MOTOR_STATE_UUID)
                if (characteristic != null) {
                    Log.d(TAG, "Subscribing to motors state...")
                    gatt.setCharacteristicNotification(characteristic, true)
                    val subscribeDescriptor = characteristic.getDescriptor(IMotors.CLIENT_CONFIG_DESCRIPTOR)
                    if (subscribeDescriptor == null) {
                        Log.w(TAG, "Failed to resolve motor state subscription descriptor.")
                    } else {
                        subscribeDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        val subs = gatt.writeDescriptor(subscribeDescriptor)
                        Log.d(TAG, "Subscribed to motors state : $subs")
                        Thread {
                            //has to wait after subscription write
                            Log.d(TAG, "Spinning motors.")
                            spinMotor(1, 127)
                            sleep(200)
                            spinMotor(1, -127)
                            sleep(200)
                            spinMotor(2, 127)
                            sleep(200)
                            spinMotor(2, -127)
                            sleep(200)
                            stopMotors()
                        }.start()
                    }
                } else {
                    Log.w(TAG, "Failed to subscribe to motor state.")
                }
            }
            Log.d(TAG, "MotorsBlueTooth connected : $servicesDiscovered")
        } else {
            Log.w(TAG, "onServicesDiscovered received: $status")
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)
        mRecievedState = characteristic.value
        Log.w(TAG, "onCharacteristicChanged received: ${mRecievedState.joinToString()}")
    }

}