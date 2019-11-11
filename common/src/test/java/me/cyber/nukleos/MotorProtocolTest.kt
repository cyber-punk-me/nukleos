package me.cyber.nukleos

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MotorProtocolTest {

    @Test
    fun testProtocolRoundtrip() {

        val message = MotorMessage(listOf(Action.Stop(), Action.Wait(1000),
                Action.Motor(byteArrayOf(0x01.toByte(), 0xFE.toByte())),
                Action.Servo(1, 10F)))

        val msgString = message.toString()

        println(msgString)

        val parsed = parseMotorMessage(msgString)

        assertEquals(msgString, parsed.toString())

    }
}