package me.cyber.nukleos.services

import me.cyber.nukleos.extensions.IEvent
import me.cyber.nukleos.services.SensorWorkService.Companion.ActionType
import me.cyber.nukleos.services.SensorWorkService.Companion.UNDEFINED

/**
 * Models for sensor events
 */

data class SensorEvent(val data: Array<Byte>?) : IEvent
data class ActionTypeEvent(@ActionType val actionType: Int = UNDEFINED) : IEvent