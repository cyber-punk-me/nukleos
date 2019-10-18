package me.cyber.nukleos.motors

import android.bluetooth.*
import android.content.Context
import android.util.Log
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.myosensor.TAG
import java.lang.Thread.sleep

class MotorsBlueTooth(private val device: BluetoothDevice) : IMotors, BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null
    private var servicesDiscovered = false
    private var connected = false
    private var speeds: ByteArray = ByteArray(IMotors.MOTORS_COUNT)

    override fun getSpeeds() = speeds

    override fun connect(context: Any) {
        if (gatt == null) {
            gatt = device.connectGatt(context as Context, false, this)
        }
    }

    fun disconnect() {
        speeds = ByteArray(IMotors.MOTORS_COUNT)
        connected = false
        gatt?.close()
        gatt = null
        servicesDiscovered = false
    }

    private fun sendSpinCommand() {
        val characteristic = gatt
                ?.getService(IMotors.SERVICE_UUID)
                ?.getCharacteristic(IMotors.CHAR_MOTOR_CONTROL_UUID)
        characteristic?.value = speeds
        gatt?.writeCharacteristic(characteristic)
    }

    override fun spinMotor(iMotor: Byte, speed: Byte) {
        speeds[iMotor - 1] = speed
        sendSpinCommand()
    }

    override fun spinMotors(speeds: ByteArray) {
        this.speeds = speeds
        sendSpinCommand()
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
                        connected = true
                        Log.d(TAG, "Subscribed to motors state : $subs")
                        Thread {
                            //has to wait after subscription write is complete
                            while(connected) {
                                Log.d(TAG, "Spinning motors.")
                                for (i in 1..IMotors.MOTORS_COUNT) {
                                    spinMotor(i.toByte(), 30)
                                    sleep(300)
                                    spinMotor(i.toByte(), -30)
                                    sleep(300)
                                    spinMotor(i.toByte(), 0)
                                    sleep(300)
                                }
                            }
                            //stopMotors()

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
        speeds = characteristic.value
        Log.w(TAG, "onCharacteristicChanged received: ${speeds.joinToString()}")
    }

}