package me.cyber.nukleos.sensors.synaps

import android.content.Context
import com.felhr.usbserial.UsbSerialDevice
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.Status

class UsbSensor(private val usbHandler: UsbHandler, private val serialPort: UsbSerialDevice) : Sensor {

    companion object {
        const val BAUD_RATE = 115200 // BaudRate. Change this value if you need
    }

    private val connectionStatusSubject: BehaviorSubject<Status> = BehaviorSubject.createDefault(Status.AVAILABLE)

    init {
        connectionStatusSubject.onNext(Status.STREAMING)
        Thread {
            startStreaming()
        }.start()
    }

    override fun getDataFlowable(): Flowable<FloatArray> {
        return usbHandler.dataFlowable()
    }

    override val name: String = "Synaps ${serialPort.deviceId}"
    override val address: String = serialPort.portName

    override fun statusObservable(): Observable<Status> = connectionStatusSubject

    private fun startStreaming() {
        write("v".toByteArray())
        Thread.sleep(1000)
        write("~4".toByteArray())
        Thread.sleep(1000)
        write("b".toByteArray())
        connectionStatusSubject.onNext(Status.STREAMING)
    }

    private fun stopStreaming() {
        write("s".toByteArray())
        connectionStatusSubject.onNext(Status.AVAILABLE)
    }

    override fun connect(context: Context) {
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

    /*
     * This function will be called from MainActivity to write data through Serial Port
     */
    private fun write(data: ByteArray) {
        serialPort.syncWrite(data, 0)
    }
}