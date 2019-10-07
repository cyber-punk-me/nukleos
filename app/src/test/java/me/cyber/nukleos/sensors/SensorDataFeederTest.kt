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

    @Test
    fun testFeederListenersWindow() {
        val listener1Data = ArrayList<List<FloatArray>>()
        val listener2Data = ArrayList<List<FloatArray>>()

        val feeder = SensorDataFeeder()
        feeder.registerSensorListener("42", object : SensorListener{
            override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                    listener1Data.add(data)
            }
        }, SubscriptionParams(4, 2))

        feeder.registerSensorListener("31", object : SensorListener{
            override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                    listener2Data.add(data)
            }
        }, SubscriptionParams(3, 1))

        assertEquals(4, feeder.size())

        feeder.onData("sensor1", listOf(floatArrayOf(1.0f), floatArrayOf(2.0f), floatArrayOf(3.0f)))
        feeder.onData("sensor1", listOf(floatArrayOf(4.0f), floatArrayOf(5.0f), floatArrayOf(6.0f)))
        feeder.onData("sensor1", listOf(floatArrayOf(7.0f), floatArrayOf(8.0f), floatArrayOf(9.0f), floatArrayOf(10.0f)))


        assertEquals(4, listener1Data.size)
            listener1Data.forEach{
                assertEquals(4, it.size)
            }

        assertEquals(listOf(1.0f, 2.0f, 3.0f, 4.0f),
                listener1Data[0].map { it[0] })

        assertEquals(listOf(3.0f, 4.0f, 5.0f, 6.0f),
                listener1Data[1].map { it[0] })

        assertEquals(listOf(5.0f, 6.0f, 7.0f, 8.0f),
                listener1Data[2].map { it[0] })

        assertEquals(listOf(7.0f, 8.0f, 9.0f, 10.0f),
                listener1Data[3].map { it[0] })


        assertEquals(8, listener2Data.size)
            listener2Data.forEach{
                assertEquals(3, it.size)
            }

        assertEquals(listOf(1.0f, 2.0f, 3.0f),
                listener2Data[0].map { it[0] })

        assertEquals(listOf(5.0f, 6.0f, 7.0f),
                listener2Data[4].map { it[0] })

        assertEquals(listOf(8.0f, 9.0f, 10.0f),
                listener2Data[7].map { it[0] })
    }


}

