package me.cyber.nukleos.sensors

import io.reactivex.Flowable
import io.reactivex.Observable

interface Sensor {
    fun getDataFlowable(): Flowable<FloatArray>

    val name: String

    val address: String

    fun statusObservable(): Observable<Status>

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

    fun isVibrationSupported(): Boolean

    fun vibration(duration: Int)

    fun getFrequency(): Int

    fun setFrequency(newFrequency: Int)

    fun getAvailableFrequencies(): List<Int>
}

enum class Status {
    AVAILABLE,
    CONNECTING,
    STREAMING
}
