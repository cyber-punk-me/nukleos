package me.cyber.nukleos.dagger
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.ui.control.SensorModel

class PeripheryManager {

    private var devicesIdCounter: Long = 0

    @Volatile var motors : IMotors? = null

    private val sensors = mutableMapOf<Long, Sensor>()
    private var activeSensor: Sensor? = null

    fun clear() {
        sensors.filter { !it.value.isConnected() }.map { it.key }.forEach {
            sensors.remove(it)
        }
        activeSensor = null
    }

    fun hasSensors() = !sensors.isEmpty()

    fun getSensorModels() = sensors.map { SensorModel(it.value.name, it.value.address, it.key) }

    fun getSensors() = sensors.values

    fun setSelectedSensorById(id: Long) {
        activeSensor = sensors[id]
    }

    fun getSelectedSensor() : Sensor? {
        return activeSensor ?: sensors.values.firstOrNull()
    }

    fun addSensor(sensor: Sensor) : Long {
        val id = devicesIdCounter++
        sensors[id] = sensor
        return id
    }
}