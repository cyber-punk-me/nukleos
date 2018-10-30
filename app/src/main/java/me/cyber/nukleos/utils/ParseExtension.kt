package me.cyber.nukleos.utils

@JvmOverloads
fun String?.safeToInt(defaultValue: Int = 0) = this?.let {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        defaultValue
    }
} ?: defaultValue