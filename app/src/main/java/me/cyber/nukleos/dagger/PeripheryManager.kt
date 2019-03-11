package me.cyber.nukleos.dagger
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.synaps.UsbHandler

class PeripheryManager {

    var synapsUsbHandler: UsbHandler? = null

    @Volatile var motors : IMotors? = null

    private val sensors = mutableListOf<Sensor>()

    private var selectedSensor: Sensor? = null

    fun clear() {
        sensors.clear()
        selectedSensor = null
    }

    fun hasSensors() = !sensors.isEmpty()

    fun getAvailableSensors() = sensors

    fun setSelectedSensor(index: Int) {
        selectedSensor = sensors[index]
    }

    fun getSelectedSensor() : Sensor? {
        return selectedSensor
    }

    fun addSensor(sensor: Sensor) {
        sensors.add(sensor)
    }
}