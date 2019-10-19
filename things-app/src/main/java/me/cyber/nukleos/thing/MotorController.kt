package me.cyber.nukleos.thing

import android.util.Log
import com.zugaldia.robocar.hardware.adafruit2348.AdafruitMotorHat
import me.cyber.nukleos.IMotors
import kotlin.math.absoluteValue

class MotorController : IMotors {

    //ada motors are indexed from 1; 0th element is empty
    private val motorsSpeeds = ByteArray(IMotors.MOTORS_COUNT)

    private val motorHat = AdafruitMotorHat(0)
    private val motorHat1 = AdafruitMotorHat(1)

    override fun getSpeeds(): ByteArray = motorsSpeeds

    override fun getConnectionStatus() = IMotors.Status.CONNECTED

    override fun getName(): String = "ADA MOTORS"

    /**
     * @param iMotor
     * @param direction
     * @param speed
     */
    override fun spinMotor(iMotor: Int, speed: Byte) {
        Log.w("Motors", "trying to spin motors.")
        val motor = when (val adaMotor = iMotor + 1) {
            in 1..MOTORS_PER_HAT -> motorHat.getMotor(adaMotor)
            in MOTORS_PER_HAT + 1..IMotors.MOTORS_COUNT -> motorHat1.getMotor(adaMotor - MOTORS_PER_HAT)
            else -> null
        }
        motorsSpeeds[iMotor] = speed
        motor!!.setSpeed(speed.toInt().absoluteValue * 2)
        if (0.toByte() != speed) {
            motor.run(if (speed > 0) AdafruitMotorHat.FORWARD else AdafruitMotorHat.BACKWARD)
        } else {
            motor.run(AdafruitMotorHat.RELEASE)
        }
    }

    override fun spinMotors(speeds: ByteArray) {
        for (iMotor in 0 until IMotors.MOTORS_COUNT) {
            spinMotor(iMotor, speeds[iMotor])
        }
    }

    override fun stopMotors() {
        for (i in 0 until IMotors.MOTORS_COUNT) {
            motorsSpeeds[i] = 0
        }
        for (adaMotor in 1..MOTORS_PER_HAT) {
            motorHat.getMotor(adaMotor).run(AdafruitMotorHat.RELEASE)
            motorHat1.getMotor(adaMotor).run(AdafruitMotorHat.RELEASE)
        }
    }

    companion object {
        val MOTORS_PER_HAT = 4
    }
}