package me.cyber.nukleos.sensors

import io.reactivex.Observable
import java.util.*
import kotlin.collections.HashMap

interface Sensor {

    val name: String

    val address: String

    fun statusObservable(): Observable<Status>

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

    fun isVibrationSupported(): Boolean

    fun vibration(duration: Int)

    fun getFrequency(): Int

    fun setFrequency(newFrequency: Int)

    fun getAvailableFrequencies(): List<Int>

    companion object {
        fun registerSensorListener(listenerName : String, sensorListener: SensorListener) {
            sensorListeners[listenerName] = sensorListener
        }

        fun removeSensorListener(listenerName : String) {
            sensorListeners.remove(listenerName)
        }

        fun onData(sensorName: String, data: FloatArray) {
            synchronized(sensorListeners) {
                sensorListeners.forEach { (_, s) -> s.onSensorData(sensorName, data) }
            }
        }

        private val sensorListeners: MutableMap<String, SensorListener>
                = Collections.synchronizedMap(HashMap<String, SensorListener>())
    }
}

interface SensorListener {
    fun onSensorData(sensorName: String, data : FloatArray)
}

enum class Status {
    AVAILABLE,
    CONNECTING,
    STREAMING
}
