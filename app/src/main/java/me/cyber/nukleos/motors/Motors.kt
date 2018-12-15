package me.cyber.nukleos.motors

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import com.nilhcem.blefun.common.MotorsInt

class Motors(private val device: BluetoothDevice) : MotorsInt, BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null


    override fun spinMotor(iMotor: Byte, direction: Byte, speed: Byte) {
        if (iMotor.toInt() == 0 && direction.toInt() == 0 && speed.toInt() == 0) {
            stopMotors()
        } else {
            //val motor = motorHat.getMotor(iMotor.toInt())
            //motor.setSpeed(speed.toInt())
            //motor.run(direction.toInt())
        }
    }


    override fun stopMotors() {
        for (i in 1..MOTOR_COUNT) {
            //motorHat.getMotor(i).run(AdafruitMotorHat.RELEASE)
        }
    }

    companion object {
        private val MOTOR_COUNT: Int = 4
    }

}