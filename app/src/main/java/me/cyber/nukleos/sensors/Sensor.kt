package me.cyber.nukleos.sensors

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable

interface Sensor {
    fun getDataFlowable(): Flowable<FloatArray>

    val name: String

    val address: String

    fun isStreaming(): Boolean

    fun statusObservable(): Observable<Status>

    fun controlObservable(): Observable<ControlStatus>

    fun startStreaming()

    fun stopStreaming()

    fun connect(context: Context)

    fun disconnect()

    fun isConnected(): Boolean

    fun isVibrationSupported(): Boolean

    fun vibration(duration: Int)

    fun getFrequency(): Int

    fun setFrequency(newFrequency: Int)

    fun getAvailableFrequencies(): List<Int>
}

enum class Status {
    CONNECTING,
    READY,
    CONNECTED,
    DISCONNECTED
}

enum class ControlStatus {
    STREAMING,
    NOT_STREAMING
}
