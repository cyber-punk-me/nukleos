package me.cyber.nukleos

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MotorProtocolTest {

    private fun timeReference() : Long = System.currentTimeMillis() % 100000
    private fun printTimeReference() {
        println("tick: ${timeReference()}")
    }

    @Test
    fun testProtocolRoundtrip() {

        val message = MotorMessage("an action", Action.Stop(), Action.Wait(1000),
                Action.Motor(byteArrayOf(0x7E.toByte(), 0x2B.toByte())),
                Action.Servo(1, 10F), Action.Servo(0, 180F))

        val msgString = message.toString()

        println(msgString)

        val parsed = parseMotorMessage(msgString)

        assertEquals(msgString, parsed.toString())

        parsed.execute(object : IMotors{

            override fun getConnectionStatus(): IMotors.Status {
                return IMotors.Status.CONNECTED
            }

            override fun spinMotor(iMotor: Int, speed: Byte) {
                println("spinMotor : $iMotor, $speed")
                printTimeReference()
            }

            override fun spinMotors(speeds: ByteArray) {
                println("spinMotors : ${String(speeds)}")
                printTimeReference()
            }

            override fun stopMotors() {
                println("stopMotors")
                printTimeReference()
            }

            override fun getSpeeds(): ByteArray {
                println("getSpeeds")
                printTimeReference()
                return byteArrayOf(0x01.toByte(), 0xFE.toByte())
            }

            override fun setServoAngle(iServo: Int, angle: Float) {
                println("setServoAngle : $iServo, $angle")
                printTimeReference()
            }
        })

        Thread.sleep(1500)

    }
}