package me.cyber.nukleos.sensors.synaps

import android.os.Handler
import android.os.Message
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.utils.showLongToast

/*
 * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
 */
class UsbHandler : Handler() {

    companion object {
        const val MILLION = 1000000
        const val frequency = 1000
    }

    /*
 *  Data received from serial port will be received here. Just populate onReceivedData with your code
 *  In this particular example. byte stream is converted to String and send to UI thread to
 *  be treated there.
 */
    private val mCallback = UsbSerialInterface.UsbReadCallback { arg0 ->
        obtainMessage(UsbService.MESSAGE_FROM_SERIAL_PORT, arg0)?.sendToTarget()
    }

    /*
     * State changes in the CTS line will be received here
     */
    private val ctsCallback = UsbSerialInterface.UsbCTSCallback {
        obtainMessage(UsbService.CTS_CHANGE)?.sendToTarget()
    }

    /*
     * State changes in the DSR line will be received here
     */
    private val dsrCallback = UsbSerialInterface.UsbDSRCallback {
        obtainMessage(UsbService.DSR_CHANGE)?.sendToTarget()
    }

    var thisSensor: Sensor? = null

    private var i = 1

    private fun getGraphData(input: IntArray): FloatArray {
        val reads = input.slice(0..7)
        //todo magic number
        return reads.map { i -> i / 80000f }.toFloatArray()
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                val data = msg.obj as ByteArray
                String(data).showLongToast()
            }
            UsbService.SYNC_READ -> {
                val data = msg.obj as ByteArray
                val packet = UsbService.readPacket(data)
                if (packet != null) {
                    val graphData = getGraphData(packet)
                    Sensor.onData(thisSensor?.name ?: "usb-sensor", listOf(graphData))
                    //Log.d("Synaps", graphData.toString())
                    if (i < frequency) {
                        i++
                    } else {
                        Log.i("Synaps", "got one second of data")
                        i = 1
                    }
                }

            }
            else -> {
                Log.w("Synaps", msg.obj.toString())
            }
        }
    }

    fun registerCallbacks(serialPort: UsbSerialDevice) {
        serialPort.read(mCallback)
        serialPort.getCTS(ctsCallback)
        serialPort.getDSR(dsrCallback)
    }


}