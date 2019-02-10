package me.cyber.nukleos.synaps

import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import me.cyber.nukleos.ui.MainActivity
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

/*
 * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
 */
class UsbHandler(val activity: MainActivity) : Handler() {

    private val dataProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()

    val frequency = 250
    val maxScale = 128.0f
    var max = 1.0f

    private fun getGraphData(input: IntArray): FloatArray {
        val reads = input.slice(0..7)
        max = max(max, reads.max()?.toFloat() ?: max)
        max = max(max, abs(reads.min()?.toFloat() ?: max))
        return reads.map { i -> (i * maxScale / max) }.toFloatArray()
    }

    fun dataFlowable(): Flowable<FloatArray> {
        return if (frequency == 0) {
            dataProcessor.onBackpressureDrop()
        } else {
            dataProcessor.sample((1000 / frequency).toLong(), TimeUnit.MILLISECONDS).onBackpressureDrop()
        }
    }

    fun isStreaming() = activity.usbStreamReady

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                val data = msg.obj as ByteArray
                Toast.makeText(activity, String(data), Toast.LENGTH_LONG).show()
            }
            UsbService.CTS_CHANGE -> Toast.makeText(activity, "CTS_CHANGE", Toast.LENGTH_LONG).show()
            UsbService.DSR_CHANGE -> Toast.makeText(activity, "DSR_CHANGE", Toast.LENGTH_LONG).show()
            UsbService.SYNC_READ -> {
                val data = msg.obj as ByteArray
                if (!activity.usbStreamReady) {
                    Toast.makeText(activity, String(data), Toast.LENGTH_LONG).show()
                    activity.usbStreamReady = true
                } else {
                    val packet = UsbService.readPacket(data)
                    if (packet != null) {
                        val graphData = getGraphData(packet)
                        dataProcessor.onNext(graphData)
                        Log.d("Synaps", graphData.toString())
                    }
                }
            }
        }
    }
}