package me.cyber.nukleos.sensors.myosensor

import java.util.*

typealias Command = ByteArray

/**
 * All protocols and other stuff you can fing here -> https://github.com/thalmiclabs/myo-bluetooth
 */
object CommandList {

    //Stop the streaming
    fun stopStreaming(): Command {
        val command_data = 0x01.toByte()
        val payload_data = 3.toByte()
        val emg_mode = 0x00.toByte()
        val imu_mode = 0x00.toByte()
        val class_mode = 0x00.toByte()
        return byteArrayOf(command_data, payload_data, emg_mode, imu_mode, class_mode)
    }

    // Start streaming (with filter)
    fun emgFilteredOnly(): Command {
        val command_data = 0x01.toByte()
        val payload_data = 3.toByte()
        val emg_mode = 0x02.toByte()
        val imu_mode = 0x00.toByte()
        val class_mode = 0x00.toByte()
        return byteArrayOf(command_data, payload_data, emg_mode, imu_mode, class_mode)
    }

    // Start streaming (without filter)
    fun emgUnfilteredOnly(): Command {
        val command_data = 0x01.toByte()
        val payload_data = 3.toByte()
        val emg_mode = 0x03.toByte()
        val imu_mode = 0x00.toByte()
        val class_mode = 0x00.toByte()
        return byteArrayOf(command_data, payload_data, emg_mode, imu_mode, class_mode)
    }

    // short signal
    fun vibration1(): Command {
        val command_vibrate = 0x03.toByte()
        val payload_vibrate = 1.toByte()
        val vibrate_type = 0x01.toByte()
        return byteArrayOf(command_vibrate, payload_vibrate, vibrate_type)
    }

    //medium signal
    fun vibration2(): Command {
        val command_vibrate = 0x03.toByte()
        val payload_vibrate = 1.toByte()
        val vibrate_type = 0x02.toByte()
        return byteArrayOf(command_vibrate, payload_vibrate, vibrate_type)
    }

    // long signal
    fun vibration3(): Command {
        val command_vibrate = 0x03.toByte()
        val payload_vibrate = 1.toByte()
        val vibrate_type = 0x03.toByte()
        return byteArrayOf(command_vibrate, payload_vibrate, vibrate_type)
    }

    // Needed to keep the Myo awake */
    fun unSleep(): Command {
        val command_sleep_mode = 0x09.toByte()
        val payload_unlock = 1.toByte()
        val never_sleep = 1.toByte() // never sleep
        return byteArrayOf(command_sleep_mode, payload_unlock, never_sleep)
    }

    // Send a sleep command
    fun normalSleep(): Command {
        val command_sleep_mode = 0x09.toByte()
        val payload_unlock = 1.toByte()
        val normal_sleep = 0.toByte()
        return byteArrayOf(command_sleep_mode, payload_unlock, normal_sleep)
    }
}
