package me.cyber.nukleos.sensors

import android.preference.PreferenceManager
import me.cyber.nukleos.App

object LastKnownSensorManager {

    private const val SENSOR_NAME_KEY = "last_known_sensor"

    fun updateLastKnownSensor(sensor: Sensor) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.applicationComponent.getAppContext())
        sharedPreferences.edit().apply {
            putString(SENSOR_NAME_KEY, sensor.name)
            apply()
        }
    }

    fun getLastKnownSensorName(): String? {
        return PreferenceManager.getDefaultSharedPreferences(App.applicationComponent.getAppContext()).getString(SENSOR_NAME_KEY, null)
    }
}