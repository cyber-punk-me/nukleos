package me.cyber.nukleos.sensors

import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.sensors.Status.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
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

        private val sensorDataFeeder: SensorDataFeeder = SensorDataFeeder()

        fun registerSensorListener(listenerName: String, sensorListener: SensorListener, subscriptionParams: SubscriptionParams = SubscriptionParams(1, 1))
                = sensorDataFeeder.registerSensorListener(listenerName, sensorListener, subscriptionParams)

        fun removeSensorListener(listenerName: String) = sensorDataFeeder.removeSensorListener(listenerName)

        fun onData(sensorName: String, data: List<FloatArray>) = sensorDataFeeder.onData(sensorName, data)

    }
}

interface SensorListener {
    //no slow operations here please
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

class SensorDataFeeder {

    private val maxWindow = AtomicInteger(1)
    private val dataQueue = LinkedList<FloatArray>()

    fun size() = synchronized(sensorListeners) { maxWindow }

    private val sensorListeners: MutableMap<String, Pair<SensorListener, SubscriptionParams>> = Collections.synchronizedMap(HashMap<String, Pair<SensorListener, SubscriptionParams>>())

    private fun updateMaxWindow() {
        val maxWindowCurrent = sensorListeners.maxBy { it.value.second.window }?.value?.second?.window ?: 1
        maxWindow.set(maxWindowCurrent)
    }

    fun registerSensorListener(listenerName: String, sensorListener: SensorListener, subscriptionParams: SubscriptionParams = SubscriptionParams(1, 1)) {
        synchronized(sensorListeners) {
            sensorListeners[listenerName] = Pair(sensorListener, subscriptionParams)
            updateMaxWindow()
        }
    }

    fun removeSensorListener(listenerName: String) {
        synchronized(sensorListeners) {
            sensorListeners.remove(listenerName)
            updateMaxWindow()
        }
    }

    /**
     * TODO : Invoke each time a window with given step is ready for this listener.
     */
    fun onData(sensorName: String, data: List<FloatArray>) {
        synchronized(sensorListeners) {
            data.forEach {
                dataQueue.push(it)
            }
            //todo chop the queue data
            sensorListeners.forEach {
                (_, s) ->
                s.first.onSensorData(sensorName, data) }

            while (dataQueue.size > maxWindow.get()) {
                dataQueue.remove()
            }
        }
    }
}