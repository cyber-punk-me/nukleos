package me.cyber.nukleos.sensors

import junit.framework.TestCase.assertEquals
import org.junit.Test


class SensorDataFeederTest {

    @Test
    fun testFeederListeners() {
        val listener1Data = ArrayList<Float>()
        val feeder = SensorDataFeeder()
        feeder.registerSensorListener("1", object : SensorListener{
            override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                data.forEach {
                    listener1Data.addAll(it.toList())
                }
            }
        })

        assertEquals(1, feeder.size())

        feeder.onData("sensor1", listOf(floatArrayOf(1.0f), floatArrayOf(2.0f), floatArrayOf(3.0f)))
        feeder.onData("sensor1", listOf(floatArrayOf(4.0f), floatArrayOf(5.0f), floatArrayOf(6.0f)))
        feeder.onData("sensor1", listOf(floatArrayOf(7.0f), floatArrayOf(8.0f), floatArrayOf(9.0f)))

        val assumedData = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f)

        assertEquals(assumedData, listener1Data)
        feeder.removeSensorListener("1")
        feeder.onData("sensor1", listOf(floatArrayOf(10.0f), floatArrayOf(11.0f), floatArrayOf(12.0f)))
        //sensor listener was removed, so no updates to listener
        assertEquals(assumedData, listener1Data)
    }


}

