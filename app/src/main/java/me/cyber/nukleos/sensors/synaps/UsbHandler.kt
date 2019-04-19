package me.cyber.nukleos.sensors.synaps

import android.os.Handler
import android.os.Message
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/*
 * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
 */
class UsbHandler : Handler() {

    companion object {
        const val MILLION = 1000000
        const val frequency = 1000
        private const val maxScaled = 127.0f
        private const val minScaled = -128.0f
        const val scale = maxScaled - minScaled

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
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


    private var i = 1

    private val dataProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()

    private fun getGraphData(input: IntArray): FloatArray {
        val reads = input.slice(0..7)
        max = max(max, reads.max()?.toFloat() ?: max)
        min = min(min, reads.min()?.toFloat() ?: max)
        return reads.map { i -> (i * scale / max(max - min, 1.0f)) }.toFloatArray()
    }

    fun dataFlowable(): Flowable<FloatArray> {
        return dataProcessor.sample((MILLION / frequency).toLong(), TimeUnit.MICROSECONDS).onBackpressureDrop()
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                //val data = msg.obj as ByteArray //TODO should we show message?
//                Toast.makeText(activity, String(data), Toast.LENGTH_LONG).show()
            }
            UsbService.SYNC_READ -> {
                val data = msg.obj as ByteArray
                val packet = UsbService.readPacket(data)
                if (packet != null) {
                    val graphData = getGraphData(packet)
                    dataProcessor.onNext(graphData)
                    Log.d("Synaps", graphData.toString())
                    if (i < frequency) {
                        i++
                    } else {
                        Log.i("Synaps", "one second")
                        i = 1
                    }
                }

            }
        }
    }

    fun registerCallbacks(serialPort: UsbSerialDevice) {
        serialPort.read(mCallback)
        serialPort.getCTS(ctsCallback)
        serialPort.getDSR(dsrCallback)
    }


}