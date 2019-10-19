package me.cyber.nukleos

import java.util.*

interface IMotors {

    fun connect(): Unit = Unit

    fun disconnect(): Unit = Unit

    fun isConnected() : Boolean = false

    /**
     * negative speed is reverse direction
     *
     * motors are indexed 1 to [motorsCount()] //todo index motors from 0
     */
    fun spinMotor(iMotor: Byte, speed: Byte)

    /**
     * set array of size [MOTORS_COUNTMOTORS_COUNT]
     * speeds will be set according to getSpeeds() structure.
     */
    fun spinMotors(speeds: ByteArray)

    fun stopMotors()

    fun motorsCount() = getSpeeds().size

    /**
     * array containing each motor speed.
     * indexed 0 to [motorsCount() - 1]
     */
    fun getSpeeds() : ByteArray

    fun getName() : String = "STUB"

    companion object {
        val MOTORS_COUNT = 8

        val SERVICE_UUID = UUID.fromString("88bb896c-3ec9-45e5-9107-d898ea6cd455")
        val CHAR_MOTOR_CONTROL_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0c")
        val CHAR_MOTOR_STATE_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0d")
        val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0e")
    }

}