package me.cyber.nukleos.dagger
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.Sensor

class PeripheryManager {

    private var devicesIdCounter: Long = 0

    @Volatile var motors : IMotors? = null

    private val sensors = mutableMapOf<Long, Sensor>()
    private var activeSensor: Sensor? = null

    val activeSensors : BehaviorSubject<Map<Long, Sensor>> = BehaviorSubject.createDefault(sensors)

    fun clear() {
        sensors.filter { !it.value.isConnected() }.map { it.key }.forEach {
            sensors.remove(it)
        }
        activeSensor = null
    }

    fun hasSensors() = !sensors.isEmpty()

    fun getSensors() = sensors.values

    fun setSelectedSensorById(id: Long) {
        activeSensor = sensors[id]
    }

    fun getSelectedSensor() : Sensor? {
        return activeSensor ?: sensors.values.firstOrNull()
    }

    fun addSensor(sensor: Sensor) {
        val id = devicesIdCounter++
        sensors[id] = sensor
        activeSensors.onNext(sensors.toMap())
    }
}