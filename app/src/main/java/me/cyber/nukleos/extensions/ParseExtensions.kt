package me.cyber.nukleos.extensions

fun String?.toFloatSafe(defaultValue: Float = -1f): Float {
    return try {
        this?.toFloat() ?: defaultValue
    } catch (e: NumberFormatException) {
        defaultValue
    }
}
