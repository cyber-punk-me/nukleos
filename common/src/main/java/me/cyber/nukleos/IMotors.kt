package me.cyber.nukleos

import java.util.*

interface IMotors {

    fun connect(): Unit = Unit

    fun disconnect(): Unit = Unit

    fun getConnectionStatus() : Status

    fun isConnected() : Boolean = Status.CONNECTED == getConnectionStatus()

    /**
     * negative speed is reverse direction
     * indexed 0 to [motorsCount() - 1]
     */
    fun spinMotor(iMotor: Int, speed: Byte)

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

    fun setServoAngle(iServo: Int, angle: Float)

    fun executeMotorMessage(motorMessage: MotorMessage) = println("Run command: $motorMessage")

    fun getName() : String = "STUB"

    enum class Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    companion object {
        val MOTORS_COUNT = 8
        val SERVOS_COUNT = 2

        val SERVICE_UUID = UUID.fromString("88bb896c-3ec9-45e5-9107-d898ea6cd455")
        val CHAR_MOTOR_CONTROL_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0c")
        val CHAR_MOTOR_STATE_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0d")
        val MOTOR_STATE_DESCRIPTOR = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0e")

        val CHAR_SERVO_CONTROL_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a1c")
        val CHAR_SERVO_STATE_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a1d")
        val SERVO_STATE_DESCRIPTOR = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a1e")

        val CHAR_MOTOR_MESSAGE_CONTROL_UUID = UUID.fromString("10992895-9b61-4380-8e51-5028d2c17a0f")

        fun writeServoCommand(iServo: Int, angle: Float) = "$iServo:$angle"

        fun readServoCommand(command: String): Pair<Int, Float> {
            val split = command.split(":")
            val iServo = Integer.parseInt(split[0])
            val angle = java.lang.Float.parseFloat(split[1])
            return Pair(iServo, angle)
        }

        val BASE_SERVO_CLOSE = 175.0 //177.0
        val BASE_SERVO_OPEN = 55.0

        val TOP_SERVO_CLOSE = 0.0
        val TOP_SERVO_OPEN = 110.0


    }

}