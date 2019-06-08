package me.cyber.nukleos.sensors.myosensor

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteReader {

    private var byteBuffer: ByteBuffer? = null

    var byteData: ByteArray? = null
        set(data) {
            field = data
            this.byteBuffer = ByteBuffer.wrap(field)
            byteBuffer?.order(ByteOrder.nativeOrder())
        }

    val short: Short
        get() = this.byteBuffer?.short ?: 0

    val byte: Byte
        get() = this.byteBuffer?.get() ?: 0

    val int: Int
        get() = this.byteBuffer?.int ?: 0

    fun rewind() = this.byteBuffer?.rewind()

    //todo make sure we always have enough bytes
    fun getBytes(size: Int): FloatArray? {
        val result = FloatArray(size)
        for (i in 0 until size)
            result[i] = byteBuffer?.get()?.toFloat() ?: 0F
        return result
    }
}
