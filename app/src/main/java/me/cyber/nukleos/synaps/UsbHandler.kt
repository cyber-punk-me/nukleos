package me.cyber.nukleos.synaps

import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import me.cyber.nukleos.ui.MainActivity
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/*
 * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
 */
class UsbHandler(val activity: MainActivity) : Handler() {

    companion object {
        val MILLION = 1000000
        val frequency = 1000
        val maxScaled = 127.0f
        val minScaled = -128.0f
        val scale = maxScaled - minScaled

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
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
        return if (frequency == 0) {
            dataProcessor.onBackpressureDrop()
        } else {
            dataProcessor.sample((MILLION / frequency).toLong(), TimeUnit.MICROSECONDS).onBackpressureDrop()
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
    }
}