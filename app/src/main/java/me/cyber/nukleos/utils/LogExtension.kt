package me.cyber.nukleos.utils

import android.widget.Toast
import me.cyber.nukleos.App


fun String.showShortToast() = Toast.makeText(App.applicationComponent.getAppContext(), this, Toast.LENGTH_SHORT).show()
fun String.showLongToast() = Toast.makeText(App.applicationComponent.getAppContext(), this, Toast.LENGTH_LONG).show()