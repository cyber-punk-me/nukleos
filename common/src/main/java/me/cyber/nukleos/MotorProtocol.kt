package me.cyber.nukleos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.Executors


const val MOTOR = "mr"
const val SERVO = "sv"
const val WAIT = "wt"
const val STOP = "sp"

@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes(JsonSubTypes.Type(value = Action.Motor::class, name = MOTOR),
        JsonSubTypes.Type(value = Action.Servo::class, name = SERVO),
        JsonSubTypes.Type(value = Action.Wait::class, name = WAIT),
        JsonSubTypes.Type(value = Action.Stop::class, name = STOP))
abstract class Action {
    abstract fun execute(motors: IMotors)

    class Motor(@get:JsonProperty("s")
                var speeds: ByteArray) : Action() {
        override fun execute(motors: IMotors) {
            motors.spinMotors(speeds)
        }

    }

    class Servo(@get:JsonProperty("s")  var iServo: Int,
                @get:JsonProperty("a")  var angle: Float): Action() {
        override fun execute(motors: IMotors) {
            motors.setServoAngle(iServo, angle)
        }

    }

    class Wait(@get:JsonProperty("t") var time: Long) : Action() {
        override fun execute(motors: IMotors) {
            Thread.sleep(time)
        }
    }

    class Stop : Action() {
        override fun execute(motors: IMotors) {
            motors.stopMotors()
        }
    }
}

val jsonMapper: ObjectMapper = jacksonObjectMapper().also {
    it.registerModule(KotlinModule())
    it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class MotorMessage(@get:JsonProperty("n") val name: String,
                        @get:JsonProperty("a") val actions: List<Action>) {

    constructor(name: String, vararg actions: Action) : this(name, actions.asList())

    fun execute(motors: IMotors) {
        Thread {
            actions.forEach { it.execute(motors) }
        }.start()
    }

    override fun toString(): String = jsonMapper.writeValueAsString(this)

}


fun parseMotorMessage(source: String) = jsonMapper.readValue(source, MotorMessage::class.java)