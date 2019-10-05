package me.cyber.nukleos.sensors

import io.reactivex.Flowable
import io.reactivex.Observable
import it.unimi.dsi.fastutil.ints.Int2IntMaps
import java.util.*
import kotlin.collections.HashMap

interface Sensor {

    fun getDataFlowable(): Flowable<FloatArray>

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
        fun registerSensorListener(name : String, sensorListener: SensorListener) {
            sensorListeners[name] = sensorListener
        }

        fun removeSensorListener(name : String) {
            sensorListeners.remove(name)
        }

        fun onData(data: FloatArray) {
            sensorListeners.forEach{n, s -> s.onSensorData(data)}
        }

        private val sensorListeners: MutableMap<String, SensorListener>
                = Collections.synchronizedMap(HashMap<String, SensorListener>())
    }
}

interface SensorListener {
    fun onSensorData(data : FloatArray)
}

enum class Status {
    AVAILABLE,
    CONNECTING,
    STREAMING
}
