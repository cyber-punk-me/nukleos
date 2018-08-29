package me.cyber.nukleos.utils

import me.cyber.nukleos.myosensor.Command


// Extension to check is a generic "start streaming" command
fun Command.isStartStreamingCommand() = this.size >= 4
        && this[0] == 0x01.toByte()
        && (this[2] != 0x00.toByte() || this[3] != 0x00.toByte() || this[4] != 0x00.toByte())

// Extension to check the is a destroy streaming command
fun Command.isStopStreamingCommand() = java.util.Arrays.equals(this, me.cyber.nukleos.myosensor.CommandList.stopStreaming())
