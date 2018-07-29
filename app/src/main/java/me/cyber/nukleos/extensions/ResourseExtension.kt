package me.cyber.nukleos.extensions

import android.content.res.Resources
import android.graphics.Color
import android.support.annotation.ColorRes
import me.cyber.nukleos.App
import me.cyber.nukleos.utils.EMPTY_STRING
import java.util.*

private val mContext by lazy {
    App.appComponent.getAppContext()
}

private val mResources by lazy {
    mContext.resources
}

@JvmOverloads
fun Int.getString(default: String = EMPTY_STRING): String = try {
    mResources.getString(this)
} catch (e: Resources.NotFoundException) {
    default
}

@ColorRes
@JvmOverloads
fun Int.getColor(default: Int = Color.BLACK): Int {
    var res: Int
    try {
        res = mResources.getColor(this)
    } catch (e: Resources.NotFoundException) {
        res = default
    }
    return res
}

fun ClosedRange<Int>.randomFloat(random: Random) = (random.nextInt((endInclusive + 1) - start) +  start).toFloat()