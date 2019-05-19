package me.cyber.nukleos.sensors.synaps

import com.felhr.usbserial.UsbSerialDevice
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.sensors.LastKnownSensorManager
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.Status

class UsbSensor(private val usbHandler: UsbHandler, private val serialPort: UsbSerialDevice) : Sensor {

    companion object {
        const val BAUD_RATE = 115200 // BaudRate. Change this value if you need

        private const val defaultDelayBetweenCommandsOverUsb = 500L

        fun startStreaming(serialPort: UsbSerialDevice) {
            write("v".toByteArray(), serialPort)
            Thread.sleep(defaultDelayBetweenCommandsOverUsb)
            write("~4".toByteArray(), serialPort)
            Thread.sleep(defaultDelayBetweenCommandsOverUsb)
            write("b".toByteArray(), serialPort)
        }

        /*
         * This function will be called from MainActivity to write data through Serial Port
         */
        private fun write(data: ByteArray, serialPort: UsbSerialDevice) {
            serialPort.syncWrite(data, 0)
        }
    }

    private val connectionStatusSubject: BehaviorSubject<Status> = BehaviorSubject.createDefault(Status.AVAILABLE)

    init {
        connectionStatusSubject.onNext(Status.STREAMING)
    }

    override fun getDataFlowable(): Flowable<FloatArray> {
        return usbHandler.dataFlowable()
    }

    override val name: String = "Synaps ${serialPort.deviceId}"
    override val address: String = serialPort.portName

    override fun statusObservable(): Observable<Status> = connectionStatusSubject

    private fun stopStreaming() {
        write("s".toByteArray(), serialPort)
        connectionStatusSubject.onNext(Status.AVAILABLE)
    }

    override fun connect() {
        LastKnownSensorManager.updateLastKnownSensor(this)
    }

    override fun disconnect() {
    }

    override fun isConnected(): Boolean = true

    override fun isVibrationSupported(): Boolean = false

    override fun vibration(duration: Int) = throw NotImplementedError()

    override fun getFrequency(): Int = getAvailableFrequencies().first()

    override fun setFrequency(newFrequency: Int) {
        throw NotImplementedError()
    }

    override fun getAvailableFrequencies(): List<Int> = listOf(1000)


    /*
     * This function will be called from MainActivity to change baud rate
     */

    fun changeBaudRate(baudRate: Int) {
        serialPort.setBaudRate(baudRate)
    }
}