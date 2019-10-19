package me.cyber.nukleos.thing

import android.util.Log
import com.zugaldia.robocar.hardware.adafruit2348.AdafruitDcMotor
import com.zugaldia.robocar.hardware.adafruit2348.AdafruitMotorHat
import me.cyber.nukleos.IMotors
import kotlin.math.absoluteValue

class MotorController : IMotors {

    //ada motors are indexed from 1; 0th element is empty
    private val motorsSpeeds = ByteArray(IMotors.MOTORS_COUNT + 1)

    private val motorHat = AdafruitMotorHat(0)
    private val motorHat1 = AdafruitMotorHat(1)

    override fun getSpeeds(): ByteArray {
        return motorsSpeeds.sliceArray(1..IMotors.MOTORS_COUNT)
    }

    override fun isConnected() = true

    override fun getName() : String = "ADA MOTORS"

    /**
     * @param iMotor
     * @param direction
     * @param speed
     */
    override fun spinMotor(iMotor: Byte, speed: Byte) {
        Log.w("Motors", "trying to spin motors.")
        val intMotor = iMotor.toInt()
        if (intMotor == 0 && speed.toInt() == 0) {
            stopMotors()
        } else {
            val motor = when (iMotor) {
                in 1..MOTORS_PER_HAT -> motorHat.getMotor(intMotor)
                in MOTORS_PER_HAT + 1..IMotors.MOTORS_COUNT -> motorHat1.getMotor(intMotor - MOTORS_PER_HAT)
                else -> null
            }
            spinMotorInner(motor!!, speed, intMotor)
        }
    }

    override fun spinMotors(speeds: ByteArray) {
        for (iMotor in 1..IMotors.MOTORS_COUNT) {
            spinMotor(iMotor.toByte(), speeds[iMotor - 1])
        }
    }

    private fun spinMotorInner(motor: AdafruitDcMotor, speed: Byte, intMotor: Int) {
        motorsSpeeds[intMotor] = speed
        motor.setSpeed(speed.toInt().absoluteValue * 2)
        if (0.toByte() != speed) {
            motor.run(if (speed > 0) AdafruitMotorHat.FORWARD else AdafruitMotorHat.BACKWARD)
        } else {
            motor.run(AdafruitMotorHat.RELEASE)
        }
    }

    override fun stopMotors() {
        for (i in 1..IMotors.MOTORS_COUNT) {
            motorsSpeeds[i] = 0
        }
        for (i in 1..MOTORS_PER_HAT) {
            motorHat.getMotor(i).run(AdafruitMotorHat.RELEASE)
            motorHat1.getMotor(i).run(AdafruitMotorHat.RELEASE)
        }
    }

    companion object {
        val MOTORS_PER_HAT = 4
    }
}