package me.cyber.nukleos.extensions

import android.widget.Toast
import me.cyber.nukleos.App

fun String.makeShortToast() = Toast.makeText(App.appComponent.getAppContext(), this, Toast.LENGTH_SHORT).show()
