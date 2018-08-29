package me.cyber.nukleos.myosensor

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
        get() = this.byteBuffer!!.short

    val byte: Byte
        get() = this.byteBuffer!!.get()

    val int: Int
        get() = this.byteBuffer!!.int

    fun rewind() = this.byteBuffer?.rewind()

    fun getBytes(size: Int): FloatArray {
        val result = FloatArray(size)
        for (i in 0 until size)
            result[i] = byteBuffer!!.get().toFloat()
        return result
    }
}
