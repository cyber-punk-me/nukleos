package me.cyber.nukleos.dagger
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.LastKnownSensorManager
import me.cyber.nukleos.sensors.Sensor

class PeripheryManager {

    private var devicesIdCounter: Long = 0

    @Volatile var motors : IMotors? = null

    private val sensors = mutableMapOf<Long, Sensor>()
    private var lastSelectedSensorId: Long? = null

    val activeSensorsObservable : BehaviorSubject<Map<Long, Sensor>> = BehaviorSubject.createDefault(sensors)

    fun removeIf(needToBeDeleted: (Sensor) -> Boolean) {
        sensors.filter { needToBeDeleted(it.value) }.map { it.key }.forEach {
            sensors.remove(it)
        }
        notifySensorsChanged()
    }

    fun hasSensors() = !sensors.isEmpty()

    fun getSensors() = sensors.values

    fun setLastSelectedSensorById(id: Long?) {
        lastSelectedSensorId = id
    }

    fun getLastSelectedSensor() : Sensor? {
        return sensors[lastSelectedSensorId] ?: sensors.values.firstOrNull()
    }

    fun addSensor(sensor: Sensor) {
        val id = devicesIdCounter++
        sensors[id] = sensor
        notifySensorsChanged()
        if (sensor.name == LastKnownSensorManager.getLastKnownSensorName()) {
            sensor.connect()
        }
    }

    fun removeSensor(id: Long) {
        sensors.remove(id)
        notifySensorsChanged()
    }

    fun getActiveSensor() = sensors.values.firstOrNull()

    private fun notifySensorsChanged() {
        activeSensorsObservable.onNext(sensors.toMap())
    }
}