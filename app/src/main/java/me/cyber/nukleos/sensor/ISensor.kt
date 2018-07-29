package me.cyber.nukleos.sensor

/**
 * Interface for wrapping Sensorthings
 */
interface ISensor {
    fun addListener(listener: ISensorListener)
    fun init():Boolean
    fun shutdown()
}