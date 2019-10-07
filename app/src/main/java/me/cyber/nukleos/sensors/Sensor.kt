package me.cyber.nukleos.sensors

import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.sensors.Status.*
import java.util.*
import kotlin.collections.HashMap

interface Sensor {

    val name: String

    val address: String

    fun statusObservable(): BehaviorSubject<Status>

    fun connect()

    fun disconnect()

    fun isConnected() : Boolean {
        return if (statusObservable().hasValue()) {
            STREAMING == statusObservable().value
        } else {
            false
        }
    }

    fun isFeedbackSupported(): Boolean

    fun feedback(param: String)

    fun getFrequency(): Int

    fun setFrequency(newFrequency: Int)

    fun getAvailableFrequencies(): List<Int>

    companion object {
        fun registerSensorListener(listenerName : String, sensorListener: SensorListener, subscriptionParams: SubscriptionParams = SubscriptionParams(1, 1)) {
            sensorListeners[listenerName] = sensorListener
        }

        fun removeSensorListener(listenerName : String) {
            sensorListeners.remove(listenerName)
        }

        /**
         * TODO : Invoke each time a window with given step is ready for this listener.
         */
        fun onData(sensorName: String, data: List<FloatArray>) {
            synchronized(sensorListeners) {
                sensorListeners.forEach {
                    (_, s) ->
                    s.onSensorData(sensorName, data) }
            }
        }

        private val sensorListeners: MutableMap<String, SensorListener>
                = Collections.synchronizedMap(HashMap<String, SensorListener>())
    }
}

interface SensorListener {
    fun onSensorData(sensorName: String, data : List<FloatArray>)
}

enum class Status {
    AVAILABLE,
    CONNECTING,
    STREAMING
}

data class SubscriptionParams(
        val window: Int,
        val slide: Int
)