package me.cyber.nukleos.motors

import android.util.Log
import me.cyber.nukleos.Action
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.MotorMessage
import me.cyber.nukleos.dagger.PeripheryManager


class MotorsControlStrategy {

    fun motorsConnected(motors: IMotors) {
        motors.executeMotorMessage(MotorMessage("connected", CLOSE_SERVOS + ALL_LIGHTS))
        standbyMode(motors)
    }

    fun control(motors: IMotors, dataClass: Int) {
        Log.d(MotorsBlueTooth.TAG, "Motors con : ${motors.isConnected()}; data : $dataClass")
        when (dataClass) {
            0 -> standbyMode(motors)
            1 -> battleMode(motors)
            2 -> hackerMode(motors)
        }
    }


    private fun battleMode(motors: IMotors) {
        Log.d(PeripheryManager.TAG, "battle")
        motors.executeMotorMessage(MotorMessage("battle",
                OPEN_SERVOS + RED)
        )
    }

    private fun standbyMode(motors: IMotors) {
        Log.d(PeripheryManager.TAG, "standby")
        motors.executeMotorMessage(MotorMessage("standby",
                CLOSE_SERVOS + ALL_LIGHTS)
        )
    }

    private fun hackerMode(motors: IMotors) {
        Log.d(PeripheryManager.TAG, "hacker")
        motors.executeMotorMessage(MotorMessage("hacker",
                CLOSE_SERVOS + GREEN)
        )
    }

    companion object {
        const val TAG = "MotorsControlStrategy"

        const val LIGHT_0 = 0.toByte()

        val RED = Action.Motor(byteArrayOf(LIGHT_0, LIGHT_0, LIGHT_0, LIGHT_0,
                (-120).toByte(), (0).toByte(), (0).toByte(), LIGHT_0))

        val GREEN = Action.Motor(byteArrayOf(LIGHT_0, LIGHT_0, LIGHT_0, LIGHT_0,
                (0).toByte(), (-120).toByte(), (0).toByte(), LIGHT_0))

        val BLUE = Action.Motor(byteArrayOf(LIGHT_0, LIGHT_0, LIGHT_0, LIGHT_0,
                (0).toByte(), (0).toByte(), (-5).toByte(), LIGHT_0))

        val ALL_LIGHTS = Action.Motor(byteArrayOf(LIGHT_0, LIGHT_0, LIGHT_0, LIGHT_0,
                //r,g,b
                (60).toByte(), (60).toByte(), (-5).toByte(), LIGHT_0))

        val OPEN_SERVOS = listOf(Action.Servo(1, IMotors.TOP_SERVO_OPEN.toFloat()),
                Action.Wait(200),
                Action.Servo(0, IMotors.BASE_SERVO_OPEN.toFloat()))

        val CLOSE_SERVOS = listOf(Action.Servo(0, IMotors.BASE_SERVO_CLOSE.toFloat()),
                Action.Wait(200),
                Action.Servo(1, IMotors.TOP_SERVO_CLOSE.toFloat()))

    }

}