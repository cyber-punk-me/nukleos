package me.cyber.nukleos

import java.util.*

interface IMotors {

    fun connect(context: Any)

    fun spinMotor(iMotor: Byte, direction: Byte, speed: Byte)

    fun stopMotors()

    companion object {
        val MOTORS_COUNT = 4

        val FORWARD: Byte = 1
        val BACKWARD: Byte = 2
        val BRAKE: Byte = 3
        val RELEASE: Byte = 4

        val SINGLE: Byte = 1
        val DOUBLE: Byte = 2
        val INTERLEAVE: Byte = 3
        val MICROSTEP: Byte = 4

        val SERVICE_UUID = UUID.fromString("88bb896c-3ec9-45e5-9107-d898ea6cd455")
        val CHAR_MOTOR_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0c")
        val CHAR_MOTOR_STATE_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0c")

    }

}