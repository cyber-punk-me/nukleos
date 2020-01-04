package me.cyber.nukleos.api

import java.util.*

enum class RunState {
    NEW, RUNNING, COMPLETE, ERROR
}

data class ScriptMeta(val scriptId: UUID, val created: Date)

data class DataMeta(val dataId: UUID, val created: Date, val updated: Date)

data class ModelMeta(val scriptId: UUID, val modelId: UUID, val dataId: UUID,
                     val state: RunState, val startTime: Date, val trainedTime: Date? = null, val log: String? = null) {
    constructor(scriptId: String, modelId: String, dataId: String, state: RunState, startTime: Date) :
            this(scriptId.toUUID(), modelId.toUUID(), dataId.toUUID(), state, startTime)
}

data class TrainModelReq(val dataId: UUID, val scriptId: UUID)

data class PredictRequest(val instances : List<List<Float>>)

data class PredictResponse(val predictions : List<Prediction>)

data class Prediction(val output: Int, val distr: List<Float>, val midLayer: List<Float>? = null)

fun String.toUUID(): UUID = UUID.fromString(this)


