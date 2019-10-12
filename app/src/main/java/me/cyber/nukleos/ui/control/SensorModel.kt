package me.cyber.nukleos.ui.control

import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.sensors.Status

data class SensorModel(val name: String, val address: String, val id: Long, val statusObs : BehaviorSubject<Status>)
