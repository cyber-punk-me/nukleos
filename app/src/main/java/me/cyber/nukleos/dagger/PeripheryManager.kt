package me.cyber.nukleos.dagger
import android.util.Log
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.sensors.LastKnownSensorManager
import me.cyber.nukleos.sensors.Sensor

class PeripheryManager {

    private var devicesIdCounter: Long = 0

    var motors : IMotors = DEFAULT_MOTORS
        set(value) {
            field = value
            notifyMotorsChanged()
        }

    private val sensors = mutableMapOf<Long, Sensor>()
    private var lastSelectedSensorId: Long? = null

    val activeSensorsObservable : BehaviorSubject<Map<Long, Sensor>> = BehaviorSubject.createDefault(sensors)

    var prevMotorsConnected : Boolean = false

    val motorsObservable: BehaviorSubject<IMotors> = BehaviorSubject.createDefault(motors).also {
        it.subscribe {
            if (prevMotorsConnected != it.isConnected()) {
                Log.i(TAG, "Motors ${it!!.getName()} ${if (it.isConnected()) "connected" else "disconnected"}")
            }
            if (it.isConnected()) {
                Log.i(TAG, "Motors state received: ${it.getSpeeds().joinToString()}")
            }

            if (it.isConnected() && !prevMotorsConnected) {
                //some random task for motors
                val theMotors = it
                Thread {
                    Log.d(TAG, "Executing motors action...")
                    //has to wait after subscription write is complete
                    while(it.isConnected()) {
                        for (i in 1..IMotors.MOTORS_COUNT) {
                            theMotors.spinMotor(i.toByte(), 30)
                            Thread.sleep(400)
                            theMotors.spinMotor(i.toByte(), -30)
                            Thread.sleep(400)
                            theMotors.spinMotor(i.toByte(), 0)
                            Thread.sleep(400)
                        }
                    }
                }.start()
            }
            prevMotorsConnected = it.isConnected()
        }
    }

    fun removeIf(needToBeDeleted: (Sensor) -> Boolean) {
        sensors.filter { needToBeDeleted(it.value) }.map { it.key }.forEach {
            sensors.remove(it)
        }
        notifySensorsChanged()
    }

    fun hasSensors() = sensors.isNotEmpty()

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

    fun notifyMotorsChanged() {
        motorsObservable.onNext(motors)
    }

    fun disconnectMotors() {
        motors.disconnect()
    }

    fun connectMotors() {
        motors.connect()
    }

    companion object {
        const val TAG = "PeripheryManager"

        val DEFAULT_MOTORS = object : IMotors{
            override fun getConnectionStatus(): IMotors.Status = IMotors.Status.DISCONNECTED

            override fun spinMotor(iMotor: Byte, speed: Byte) {
            }
            override fun spinMotors(speeds: ByteArray) {
            }
            override fun stopMotors() {
            }
            override fun getSpeeds(): ByteArray = ByteArray(IMotors.MOTORS_COUNT)
        }
    }
}