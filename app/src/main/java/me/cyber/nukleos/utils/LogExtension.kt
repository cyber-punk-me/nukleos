package me.cyber.nukleos.utils

import android.widget.Toast
import me.cyber.nukleos.App

private var currentToast: Toast? = null

fun String.showShortToast() {
    currentToast?.cancel()
    val toast = Toast.makeText(App.applicationComponent.getAppContext(), this, Toast.LENGTH_SHORT)
    toast.show()
    currentToast = toast
}

fun String.showLongToast() {
    currentToast?.cancel()
    val toast = Toast.makeText(App.applicationComponent.getAppContext(), this, Toast.LENGTH_LONG)
    toast.show()
    currentToast = toast
}