package me.cyber.nukleos.thing

import android.util.Log
import com.google.android.things.contrib.driver.pwmservo.Servo
import com.zugaldia.robocar.hardware.adafruit2348.AdafruitMotorHat
import me.cyber.nukleos.IMotors
import kotlin.math.absoluteValue

class MotorController : IMotors {

    //ada motors are indexed from 1; 0th element is empty
    private val motorsSpeeds = ByteArray(IMotors.MOTORS_COUNT)

    private var motorHat = AdafruitMotorHat(0)
    private val motorHat1 = AdafruitMotorHat(1)
    //base, pin  18
    private val servo0 = initServo("PWM0", 180.0, 60.0, 180.0)
    //upper, pin  13
    private val servo1 = initServo("PWM1", 0.0, 0.0, 120.0)

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

    override fun setServoAngle(iServo: Int, angle: Float) {
        val servo = when(iServo) {
            0 -> servo0
            else -> servo1
        }
        servo.angle = angle.toDouble()
        Log.d(getName(), "Set servo $iServo to angle ${servo.angle}")
    }

    private fun initServo(pwmPin: String, startAngle: Double, minAngle: Double, maxAngle: Double): Servo = Servo(pwmPin).also {
        it.setPulseDurationRange(0.5, 2.5)
        it.setAngleRange(minAngle, maxAngle)
        it.angle = startAngle
        it.setEnabled(true)
    }

    companion object {
        val MOTORS_PER_HAT = 4
    }
}