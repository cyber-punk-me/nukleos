package me.cyber.nukleos.ui.dashboard

import android.databinding.ObservableField

class SensorListItemViewModel(val sensorName: String = "Sensor Name" ){
    val sensorValue: ObservableField<String> = ObservableField<String>("value")
}