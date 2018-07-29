package me.cyber.nukleos.sensor

import java.util.*

interface ISensorListener {
        fun onCharacteristicChanged(sensor: ISensorSettings, var2: UUID, var3: ByteArray)
}